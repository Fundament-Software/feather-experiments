

import .nkc-raw
import C.stdio
using import itertools
using import spicetools
using import String

let handle-sym = (Symbol "#nuklear-handle")
let ctx-sym = (Symbol "#nuklear-context")
let arg-sym = (Symbol "#nuklear-arg")

inline print-pass (x)
    print x;
    x

inline genwrapper (func)
    sugar wrapper (args...)
        # vvv print-pass
        # try
            qq [func] [ctx-sym]
                unquote-splice args...
        # else
            error
                ..
                    "error expanding genwrapper for "
                    tostring func

            _;
        let scope = sugar-scope
        try
            let ctx = ('@ scope ctx-sym)
            # if (('typeof ctx) != (mutable@ nkc-raw.nk_context))
            #     error
            #         ..
            #             "nuklear wrapper called in scope with the wrong type of context, expected\n"
            #             tostring (mutable@ nkc-raw.nk_context)
            #             "\nbut got\n"
            #             tostring ('typeof ctx)

            qq [func] [ctx-sym]
                unquote-splice args...
        except (exc)
            error@+ exc unknown-anchor
                ..
                    "while expanding wrapper for\n"
                    tostring func


fn underscorify (prefix sym)
    let name = (sym as Symbol as string)
    let repl =
        ->> name
            map
                inline (c)
                    match c
                    case 45
                        95:i8
                    default
                        c
            string.collector (countof name)
    Symbol (prefix .. repl)

sugar wrap-fns (syms...)
    qq [let]
        unquote-splice syms...
        =
        unquote-splice
            vvv 'reverse
            ->> syms...
                map
                    inline (sym)
                        let name = (underscorify "nk_" sym)
                        let func = ('@ nkc-raw name)
                        qq
                            [genwrapper] [func]
                'cons-sink '()

sugar gen-methods (prefix syms...)
    let prefix = (prefix as string)
    qq [let]
        unquote-splice syms...
        =
        unquote-splice
            vvv 'reverse
            ->> syms...
                map
                    inline (sym)
                        let name = (underscorify prefix sym)
                        let func = ('@ nkc-raw name)
                        qq
                            [func]
                'cons-sink '()

sugar wrap-consts (syms...)
    qq [let]
        unquote-splice syms...
        =
        unquote-splice
            vvv 'reverse
            ->> syms...
                map
                    inline (sym)
                        let name = (underscorify "NK_" sym)
                        let val = ('@ nkc-raw name)
                        qq [val]
                'cons-sink '()

sugar wrapped-inline (name (args...) body...)
    let declaration? = (('typeof name) == Symbol)
    if declaration?
        qq
            [let] [name] =
                [genwrapper]
                    [inline] [name] ([ctx-sym] (unquote-splice args...))
                        unquote-splice body...
    else
        qq
            [genwrapper]
                [inline] [name] ([ctx-sym] (unquote-splice args...))
                    unquote-splice body...

inline file-line-of (arg)
    let line = (sc_anchor_lineno ('anchor arg))
    let path = (sc_anchor_path ('anchor arg))
    let loc = (.. (tostring path) ":" (tostring line))
    `loc
inline line-of (arg)
    let line = (sc_anchor_lineno ('anchor arg))
    `line

run-stage;

module :=
    do
        nkc := nkc-raw.nkc
        context := nkc-raw.nk_context

        vec2 := nkc-raw.nk_vec2
        rect := nkc-raw.nk_rect

        color := nkc-raw.nk_color
        rgb := nkc-raw.nk_rgb

        image := nkc-raw.nk_image

        wrap-fns
            window-get-position
            window-get-size
            window-get-bounds
            window-get-width
            window-get-height
            window-get-panel
            window-get-content-region
            window-get-content-region-min
            window-get-content-region-max
            window-get-content-region-size
            window-get-canvas
            # window-get-scroll
            window-has-focus
            window-is-hovered
            window-is-collapsed
            window-is-closed
            window-is-hidden
            window-is-active
            window-is-any-hovered

        sugar make-panel-flags (flags...)
            let flagvals =
                vvv 'reverse
                ->> flags...
                    map
                        inline (sym)
                            let name = (underscorify "NK_WINDOW_" sym)
                            let val = ('@ nkc-raw name)
                            qq [val]
                    'cons-sink '()
            qq [|] (unquote-splice flagvals)

        wrap-consts
            TEXT_ALIGN_LEFT
            TEXT_ALIGN_CENTERED
            TEXT_ALIGN_RIGHT
            TEXT_ALIGN_TOP
            TEXT_ALIGN_MIDDLE
            TEXT_ALIGN_BOTTOM
            TEXT_LEFT
            TEXT_CENTERED
            TEXT_RIGHT

        wrap-fns
            text
            text-colored
            text-wrap
            text-wrap-colored
            label
            label-colored
            label-wrap

        show :=
            genwrapper
                fn... "show"
                case (ctx : (mutable@ context), str : (zarray i8), align : u32) (nkc-raw.nk_text ctx str (i32 (countof str)) align)
                case (ctx : (mutable@ context), str : (zarray i8)) (this-function ctx str TEXT_LEFT)
                case (ctx : (mutable@ context), str : (zarray i8), align : u32, col : color) (nkc-raw.nk_text_colored ctx str (i32 (countof str)) align col)
                case (ctx : (mutable@ context), str : (zarray i8), col : color) (this-function ctx str TEXT_LEFT col)
                case (ctx : (mutable@ context), str : String, align : u32) (nkc-raw.nk_text ctx str (i32 (countof str)) align)
                case (ctx : (mutable@ context), str : String) (this-function ctx str TEXT_LEFT)
                case (ctx : (mutable@ context), str : String, align : u32, col : color) (nkc-raw.nk_text_colored ctx str (i32 (countof str)) align col)
                case (ctx : (mutable@ context), str : String, col : color) (this-function ctx str TEXT_LEFT col)
                case (ctx : (mutable@ context), str : string, align : u32) (nkc-raw.nk_text ctx str (i32 (countof str)) align)
                case (ctx : (mutable@ context), str : string) (this-function ctx str TEXT_LEFT)
                case (ctx : (mutable@ context), str : string, align : u32, col : color) (nkc-raw.nk_text_colored ctx str (i32 (countof str)) align col)
                case (ctx : (mutable@ context), str : string, col : color) (this-function ctx str TEXT_LEFT col)
                case (ctx : (mutable@ context), img : image) (nkc-raw.nk_image_show ctx img)

        wrap-fns
            # button-text
            # button-label
            # button-color
            # button-symbol
            # button-image
            # button-symbol-label
            # button-symbol-text
            # button-image-label
            # button-image-text
            # button-text-styled
            # button-label-styled
            # button-symbol-styled
            # button-image-styled
            # button-symbol-text-styled
            # button-symbol-label-styled
            # button-image-label-styled
            # button-image-text-styled
            button-set-behavior
            button-push-behavior
            button-pop-behavior

        let symbol = nkc-raw.nk_symbol_type
        # todo, make these fields of the symbol type
        wrap-consts
            SYMBOL_NONE
            SYMBOL_X
            SYMBOL_UNDERSCORE
            SYMBOL_CIRCLE_SOLID
            SYMBOL_CIRCLE_OUTLINE
            SYMBOL_RECT_SOLID
            SYMBOL_RECT_OUTLINE
            SYMBOL_TRIANGLE_UP
            SYMBOL_TRIANGLE_DOWN
            SYMBOL_TRIANGLE_LEFT
            SYMBOL_TRIANGLE_RIGHT
            SYMBOL_PLUS
            SYMBOL_MINUS
            SYMBOL_MAX

        collapse-state := nkc-raw.nk_collapse_states
        type+ collapse-state
            inline... __typecall
            case (cls)
                nkc-raw.NK_MINIMIZED
            case (cls, val : bool)
                ? val nkc-raw.NK_MAXIMIZED nkc-raw.NK_MINIMIZED


        sugar tree ((treetype title state id...) body...)
            let idargs =
                sugar-match id...
                case ()
                    qq [(file-line-of treetype)] ([countof] [(file-line-of treetype)]) [(line-of treetype)]
                case (id)
                    qq [(file-line-of treetype)] ([countof] [(file-line-of treetype)]) [id]
                case (buff len)
                    qq [buff] [len] [sc_anchor_lineno ('anchor treetype)]
                case (buff len seed)
                    qq [buff] [len] [seed]
                default
                    error "invalid syntax; tree-push must recieve between three and six arguments"
            let treeflag =
                if (treetype == 'node)
                    qq [nkc-raw.NK_TREE_NODE]
                elseif (treetype == 'tab)
                    qq [nkc-raw.NK_TREE_TAB]
                else
                    error "the tree type must be either node or tab"
            inline... tree_push
            case (ctx : (mutable@ context), tree-type : nkc-raw.nk_tree_type, title, state : bool, id...)
                nkc-raw.nk_tree_push_hashed ctx tree-type title (? state nkc-raw.NK_MAXIMIZED nkc-raw.NK_MINIMIZED) id...
            case (ctx : (mutable@ context), tree-type : nkc-raw.nk_tree_type, title, state : nkc-raw.nk_collapse_states, id...)
                nkc-raw.nk_tree_state_push ctx tree-type title &state
            qq
                [embed]
                    [if] ([tree_push] [ctx-sym] [treeflag] [title] [state] (unquote-splice idargs))
                        unquote-splice body...
                        [nkc-raw.nk_tree_pop] [ctx-sym]
        tree-push :=
            genwrapper
                fn... "tree-push"
                case (ctx : (mutable@ context), treetype : bool, title : (zarray i8), state : bool)




        fn... button-any-raw
        case (ctx, str : (zarray i8))
            nkc-raw.nk_button_text ctx str ((countof str) as i32)
        case (ctx, col : color)
            nkc-raw.nk_button_label ctx col
        case (ctx, sym : nkc-raw.nk_symbol_type)
            nkc-raw.nk_button_symbol ctx sym
        case (ctx, img : image)
            nkc-raw.nk_button_image ctx img
        case (ctx, sym : nkc-raw.nk_symbol_type, str : (zarray i8), align : u32)
            nkc-raw.nk_button_symbol_text ctx sym str ((countof str) as i32) align
        case (ctx, img : image, str : (zarray i8), align : u32)
            nkc-raw.nk_button_image_text ctx img str ((countof str) as i32) align
        case (ctx, style : (@ nkc-raw.nk_style_button), str : (zarray i8), align)
            nkc-raw.nk_button_text_styled ctx style str ((countof str) as i32)
        case (ctx, style : (@ nkc-raw.nk_style_button), sym : nkc-raw.nk_symbol_type)
            nkc-raw.nk_button_symbol_styled ctx style sym
        case (ctx, style : (@ nkc-raw.nk_style_button), img : image)
            nkc-raw.nk_button_image_styled ctx style img
        case (ctx, style : (@ nkc-raw.nk_style_button), sym : nkc-raw.nk_symbol_type, str : (zarray i8), align : u32)
            nkc-raw.nk_button_symbol_text_styled ctx style sym str ((countof str) as i32) align
        case (ctx, style : (@ nkc-raw.nk_style_button), img : image, str : (zarray i8), align : u32)
            nkc-raw.nk_button_image_text_styled ctx style img str ((countof str) as i32) align

        button-any := (genwrapper button-any-raw)

        sugar button (args body...)
            args as:= list
            qq
                [embed]
                    [if] ([button-any-raw] [ctx-sym] (unquote-splice args))
                        unquote-splice body...
        unlet button-any-raw

        input := nkc-raw.nk_input
        get-input := (genwrapper (fn "get-input" (ctx) ctx.input))
        type+ input
            gen-methods "nk_input_"
                has-mouse-click
                has-mouse-click-in-rect
                has-mouse-click-down-in-rect
                is-mouse-click-in-rect
                is-mouse-click-down-in-rect
                any-mouse-click-in-rect
                is-mouse-prev-hovering-rect
                is-mouse-hovering-rect
                mouse-clicked
                is-mouse-down
                is-mouse-pressed
                is-mouse-released
                is-key-pressed
                is-key-released
                is-key-down

        layout-row-dynamic := (genwrapper nkc-raw.nk_layout_row_dynamic)

        WIN_NORMAL := nkc-raw.NKC_WIN_NORMAL

        command-buffer := nkc-raw.nk_command_buffer

        contextual-item :=
            genwrapper
                fn... "contextual-item"
                case (ctx : (@ context), str : (zarray i8), align : u32) (nkc-raw.nk_contextual_item_text ctx str (countof str) align)
                case (ctx : (@ context), img : image, str : (zarray i8), align : u32) (nkc-raw.nk_contextual_item_image_text ctx img str (countof str) align)
                case (ctx : (@ context), sym : nkc-raw.nk_symbol_type, str : (zarray i8), align : u32) (nkc-raw.nk_contextual_item_symbol_text ctx sym str (countof str) align)
        contextual-close := (genwrapper nkc-raw.nk_contextual_close)
        sugar contextual (flags size bounds body...)
            qq
                [do]
                    [fn...] item
                    case () # TODO: finish this

        sugar layout-row-template (height body...)
            qq
                [do]
                    [let] push-dynamic = [(genwrapper nkc-raw.nk_layout_row_template_push_dynamic)]
                    [let] push-static = [(genwrapper nkc-raw.nk_layout_row_template_push_static)]
                    [let] push-variable = [(genwrapper nkc-raw.nk_layout_row_template_push_variable)]
                    [nkc-raw.nk_layout_row_template_begin] [ctx-sym] [height]
                    unquote-splice body...
                    [nkc-raw.nk_layout_row_template_end] [ctx-sym]

        sugar group (title flags body...)
            qq
                [embed]
                    [if] ([nkc-raw.nk_group_begin] [ctx-sym] [title] [flags])
                        unquote-splice body...
                        [nkc-raw.nk_group_end] [ctx-sym]


        #sugar layout-space

        wrap-fns
            stroke-line
            stroke-curve
            stroke-rect
            stroke-circle
            stroke-arc
            stroke-triangle
            stroke-polyline
            stroke-polygon
            fill-rect
            fill-rect-multi-color
            fill-circle
            fill-arc
            fill-triangle
            fill-polygon
            draw-image
            # draw-nine-slice
            draw-text
            push-scissor
            push-custom

        sugar button-label (text body...)
            qq
                [embed]
                    [if] ([nkc-raw.nk_button_label] [ctx-sym] [text])
                        unquote-splice body...

        sugar window (name rect flags body...)
            # print "in window" name
            # vvv print-pass
            qq
                [embed]
                    [if] ([nkc-raw.nk_begin] [ctx-sym] [name] [rect] [flags])
                        unquote-splice body...
                    [nkc-raw.nk_end] [ctx-sym]

        sugar ui (name (application) body...)
            # vvv print-pass
            qq
                [fn] [name] ([handle-sym] [application])
                    [let] [ctx-sym] = ([nkc-raw.nkc_get_ctx] [handle-sym])
                    # (print "inside generated ui function")

                    [local] e = ([nkc-raw.nkc_poll_events] [handle-sym])

                    [embed]
                        [if] ([and] (e.type == [nkc-raw.NKC_EWINDOW]) (e.window.param == [nkc-raw.NKC_EQUIT]))
                            ([C.stdio.printf] "attempting to stop main loop\n")
                            [nkc-raw.nkc_stop_main_loop] [handle-sym]
                            [return]
                    unquote-splice body...
                    [nkc-raw.nkc_render] [handle-sym] ([rgb] 40 40 40)

        sugar template (name (args...) body...)
            let declaration? = (('typeof name) == Symbol)
            let text-name =
                if declaration?
                    name as Symbol as string
                else
                    name as string
            # let fngen =
            #     qq
            #         [fn] [text-name] ([ctx-sym] (unquote-splice args...))
            #             unquote-splice body...

            # vvv print-pass
            # if declaration?
            #     qq ([let] [name] = ([genwrapper] [fngen]))
            # else
            #     qq ([genwrapper] [fngen])

            # vvv print-pass
            # qq
            #     [inline] [name] ((unquote-splice args...))
            #         unquote-splice body...

            vvv print-pass
            qq
                [wrapped-inline] [name] ((unquote-splice args...))
                    unquote-splice body...

        sugar subui (name (args...) body...)
            qq
                [fn] [name] ([ctx-sym] (unquote-splice args...))
                    unquote-splice body...
        sugar subui-method (name (self args...) body...)
            qq
                [fn] [name] ([self] [ctx-sym] (unquote-splice args...))
                    unquote-splice body...
        sugar call-subui (f args...)
            spice invoke-subui (f ctx args...)
                if (('typeof f) == Symbol)
                    spice-match args...
                    case (obj rest...)
                        `(f obj ctx rest...)
                    default
                        error "subui method call must have an object"
                else
                    `(f ctx args...)
            qq
                [invoke-subui] [f] [ctx-sym] (unquote-splice args...)

        inline start (app ui title w h flags)
            local nkcx = (nkc)
            # local arg = (tupleof &nkcx app)
            if (!= (nkc-raw.nkc_init &nkcx title w h flags) null)
                C.stdio.printf "successful init, starting main loop\n"
                nkcx.keepRunning = true;
                loop ()
                    if (!= nkcx.keepRunning 0)
                        ui &nkcx app
                    else
                        C.stdio.printf "breaking loop\n"
                        break;
                # nkc-raw.nkc_set_main_loop &nkcx ui (&arg as voidstar)
            nkc-raw.nkc_shutdown &nkcx
            C.stdio.printf "ran shutdown\n"
        nkc_init := nkc-raw.nkc_init

        locals;


module
