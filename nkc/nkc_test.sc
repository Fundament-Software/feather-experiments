
using import .nkc-raw
using import struct
using import enum

print nkc

struct MyApp plain
    nkcHandle : (mutable (@ nkc))
    value : f32

fn mainLoop (arg)
    let myapp = (arg as (mutable (@ MyApp)))
    let ctx = (nkc_get_ctx myapp.nkcHandle)

    (print "attempting to poll events")
    local e = (nkc_poll_events myapp.nkcHandle)
    (dump e)
    (print e)

    if (and (e.type == NKC_EWINDOW) (e.window.param == NKC_EQUIT))
        (print "attempting to stop main loop")
        nkc_stop_main_loop myapp.nkcHandle

    if (nk_begin ctx "Show" (nk_rect 50 50 220 220) 0)
        nk_layout_row_static ctx 30 80 1
        if (nk_button_label ctx "button")
            print "Button pressed"


    nk_end ctx
    nkc_render myapp.nkcHandle (nk_rgb 40 40 40)

fn main ()
    local myapp = (MyApp)
    local nkcx = (nkc)
    myapp.nkcHandle = &nkcx
    myapp.value = 0.4

    local ok : (mutable (@ nk_context))
    ok = (nkc_init myapp.nkcHandle "Nuklear+ Example" 640 480 NKC_WIN_NORMAL)
    if (!= ok null)
        print "successful init, starting main loop"
        nkc_set_main_loop myapp.nkcHandle mainLoop (&myapp as voidstar)

    nkc_shutdown myapp.nkcHandle
    return 0

(main)
