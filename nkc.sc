

import .nkc-raw

let handle-sym = (Symbol "#nuklear-handle")
let ctx-sym = (Symbol "#nuklear-context")
let arg-sym = (Symbol "#nuklear-arg")

inline print-pass (x)
    print x;
    x

inline genwrapper (func)
    sugar wrapper (args...)
        # vvv print-pass
        qq [func] [ctx-sym]
            unquote-splice args...



run-stage;

module :=
    do
        nkc := nkc-raw.nkc
        context := nkc-raw.nk_context

        text := (genwrapper nkc-raw.nk_label)

        recti := nkc-raw.nk_recti
        rectf := nkc-raw.nk_rectf

        rgb := nkc-raw.nk_rgb

        WIN_NORMAL := nkc-raw.NKC_WIN_NORMAL
        TEXT_LEFT := nkc-raw.NK_TEXT_LEFT

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
                    [dump] [handle-sym]
                    [let] [ctx-sym] = ([nkc-raw.nkc_get_ctx] [handle-sym])
                    # (print "inside generated ui function")

                    [local] e = ([nkc-raw.nkc_poll_events] [handle-sym])

                    [embed]
                        [if] ([and] (e.type == [nkc-raw.NKC_EWINDOW]) (e.window.param == [nkc-raw.NKC_EQUIT]))
                            (print "attempting to stop main loop")
                            [nkc-raw.nkc_stop_main_loop] [handle-sym]
                            [return]
                    unquote-splice body...
                    [nkc-raw.nkc_render] [handle-sym] ([rgb] 40 40 40)

        inline start (app ui title w h flags)
            local nkcx = (nkc)
            dump nkcx
            dump &nkcx
            print nkcx.nkcInited
            print &nkcx
            local arg = (tupleof &nkcx app)
            if (!= (nkc-raw.nkc_init &nkcx title w h flags) null)
                print "successful init, starting main loop"
                nkcx.keepRunning = true;
                loop ()
                    if (!= nkcx.keepRunning 0)
                        ui &nkcx app
                    else
                        print "breaking loop"
                        break;
                # nkc-raw.nkc_set_main_loop &nkcx ui (&arg as voidstar)
            nkc-raw.nkc_shutdown &nkcx
            print "ran shutdown"
        nkc_init := nkc-raw.nkc_init

        locals;


module
