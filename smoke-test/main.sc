
using import ..nkc
using import C.stdio


ui main-ui (state)
    window "test window" (rect 50 50 220 220) (make-panel-flags BORDER MOVABLE SCALABLE)
        layout-row-dynamic 30 1
        show "Some sample text." TEXT_LEFT
        show "Some other text." TEXT_LEFT
        button ("test button")
            printf "the button was pressed\n"

fn main()
    local state = 0.5
    start state main-ui "Test code" 640 480 WIN_NORMAL

main
