
using import .nkc
using import C.stdio


ui main-ui (state)
    window "test window" (rectf 50 50 220 220) 0
        layout-row-dynamic 30 1
        text "Some sample text." TEXT_LEFT
        text "Some other text." TEXT_LEFT
        button-label "test button"
            printf "the button was pressed\n"

fn main()
    local state = 0.5
    start state main-ui "Test code" 640 480 WIN_NORMAL

main
