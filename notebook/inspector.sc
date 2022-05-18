
using import ..nkc
using import struct
using import Option
using import Array
using import itertools
using import Capture
using import Map

struct inspector-state
    expanded : collapse-state
    children : (Array this-type)
    childidx : u32

    inline start (self)
        self.childidx = 0
    inline next (self)
        if (self.childidx < (countof self.children))
            let x = (@ self.children self.childidx)
            self.childidx += 1
            'start x
            x
        else
            'append self.children (this-type)
            self.childidx += 1
            'last self.children


let hook-type = (@ (void <-: ((mutable@ context) Value (viewof (mutable& inspector-state) 3))))
inline hookify (x)
    static-typify x (mutable@ context) Value (mutable& inspector-state)

inline value-inspector (val state op)
    global handlers : (Map type hook-type)
    let typ = ('typeof val)
    try
        (('get handlers typ) val state)
    else
        fn generated-inspector (ctx val_prime state)
            try
                val_prime as:= typ
                op val_prime state
            else
                report "an error occurred in generated inspector"
        let f = (hookify generated-inspector)
        'set handlers typ f
        f val state



global inspector-hooks : (Array hook-type)

subui inspect (val state)
    returning void
    for hook in inspector-hooks
        call-subui hook val state

inspector-hooks =
    (Array hook-type)
        hookify
            subui "inspect-typeof" (val state)
                let my-state = ('next state)
                let t = ('typeof val)
                tree (node ("typeof : " .. (tostring t)) my-state.expanded)
                    call-subui inspect t my-state
                _;
        hookify
            subui "inspect-symbols" (val state)
                if (('typeof val) == type)
                    try
                        let t = (val as type)
                        if ((countof ('symbols t)) > 0)
                            let methods-state = ('next state)
                            tree (node "symbols" methods-state.expanded)
                                layout-row-template 12
                                    push-static 200
                                    push-dynamic;
                                for m in ('symbols t)
                                    show (tostring m)
                                    show (tostring ('typeof ('@ t m)))
                    else
                        show "an error occurred"
                _;
        # hookify
            subui "smoke-test" (val state)
                show "test value"
        hookify
            subui "inspect-type-fields" (val state)
                if (('typeof val) == type)
                    try
                        let t = (val as type)
                        let fields = ('@ t '__fields__)
                        # report ('typeof fields)
                        let my-state = ('next state)
                        tree (node "__fields__" my-state.expanded)
                            for x in ('args fields)
                                let fld = (('@ (x as type) 'Type) as type)
                                let sym typ = ('keyof fld)
                                let field-state = ('next my-state)
                                tree (node (.. (tostring sym) " : " (tostring typ)) field-state.expanded)
                                    call-subui inspect typ field-state
                    else
                        _;
        hookify
            subui "inspect-type-supers" (val state)
                if (('typeof val) == type)
                    try
                        let t = (val as type)
                        if (('superof t) != t)
                            let super-state = ('next state)
                            tree (node (.. "supertype : " (tostring ('superof t))) super-state.expanded)
                                call-subui inspect ('superof t) ('next super-state)
                    else (_)
        # hookify
            subui "inspect-value-fields" (val state)
                value-inspector val state
                    spice (val state)
                        # let fields = ('@ typ '__fields__)
                        report val

                        # for x in ('args fields)
                            x as:= type
                            x := ('@ x 'Type)
                            x as:= type
                            let sym typ = ('keyof x)
                            let fld = (getattr val sym)
                            show (.. (tostring sym) " = " (tostring fld))
                else (_)
        # hookify
            subui "inspect-value-elements" (val state)
                let typ = ('typeof val)
                try
                    let count = (countof val)
                    show (tostring count)
                else
                    show "no elements"
        hookify
            subui "inspect-anchor" (val state)
                show
                    ..
                        "anchor : "
                        tostring ('anchor val)


struct inspector
    val : (viewof Value)
    state : inspector-state

    subui-method render (self)
        'start self.state
        call-subui inspect self.val self.state
        # tree (node (tostring self.val) true)
            tree (node "typeof" false)
                let t = ('typeof self.val)
                call-subui inspect-type t self.state


locals;
