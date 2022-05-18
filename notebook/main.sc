
using import ..nkc
using import struct
using import Array
using import Map
using import String
using import Capture
using import .inspector

struct codecell
    code : String

    fn evaluate (self notebook)
        print "evaluating code"

    fn __repr (self)
        ..
            "[code="
            repr self.code
            "]"

    subui-method render (self notebook)
        (layout-row-dynamic 0 1)
        show "this is a code cell"
        show self.code

struct resultcell

struct notebook
    cells : (Array codecell)
    ins : inspector

    subui-method render (self)
        for cell in self.cells
            call-subui 'render cell self
        call-subui 'render self.ins

struct application
    nb : notebook


ui render-application (self)
    window "notebook" (rect 20 20 600 1000) (make-panel-flags BORDER MOVABLE SCALABLE)
        show "menu goes here"
        call-subui 'render self.nb

fn main ()
    local app : application
        nb =
            notebook
                cells =
                    (Array codecell)
                        codecell
                            "(print \"hello world\")"
                ins =
                    inspector
                        val = (fn (foo bar) (+ foo 1 bar))

    print app.nb.cells
    let cellref = (@ app.nb.cells 0)
    print cellref
    print cellref.code

    start app render-application "Notebook" 640 480 WIN_NORMAL
