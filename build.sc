
import .main
import .nkc-raw

fn c-main (argc argv)
    main;

let c-main = (static-typify c-main i32 (pointer rawstring))

let scope =
    'bind-symbols (Scope)
        main = c-main

compile-object
    default-target-triple
    compiler-file-kind-object
    module-dir .. "/main.o"
    scope
