package junghyun.ai.engin;

class AISetting {

    static int DEF_AI_POINT = 2; //AI 돌 근처에 부여되는 포인트
    static int DEF_PLAYER_POINT = 1; //플레이어 돌 근처에 부여되는 포인트


    static int WIN_5_POINT = 100000; //놓는 즉시 승리
    static int LOSE_5_POINT = 10000; //상대가 놓는 즉시 패배, 5 생성

    static int PLAYER_MAKE_OPEN_4_POINT = 1700; //상대가 열린 4를 만들수 있는 포인트
    static int PLAYER_MAKE_CLOSE_4_POINT = 50; //상대가 닫힌 4를 만들수 있는 포인트
    static int PLAYER_MAKE_OPEN_3_POINT = 60; //상대가 열린 3을 만들수 있는 포인트

    static int PLAYER_MAKE_4_3_POINT = 100; //상대가 4-3을 만들수 있는 포인트
    static int PLAYER_MAKE_4_4_POINT = 1000; //상대가 4-4을 만들수 있는 포인트

    static int MAKE_VT_POINT = 1000; //연속적인 수로 4-3 또는 4-4를 만들수 있는 포인트

    static int MAKE_OPEN_4_POINT = 4000; //열린 4를 만들수 있는 포인트
    static int MAKE_CLOSE_4_POINT = 70; //닫힌 4를 만들수 있는 포인트
    static int MAKE_OPEN_3_POINT = 80; //열린 3을 만들수 있는 포인트

    static int MAKE_4_3_POINT = 1800; //4-3을 만들수 있는 포인트
    static int MAKE_4_4_POINT = 2000; //4-4을 만들수 있는 포인트


    final static int[] THREE_CASE_1 = {1, 1, 0, 0}; //●●○○

    final static int[] THREE_CASE_2 = {1, 0, 1, 0}; //●○●○

    final static int[] THREE_CASE_3 = {1, 0, 0, 1}; //●○○●


    final static int[] FOUR_CASE_1_L = {1, 1, 0, 0, 1}; //●●○○●
    final static int[] FOUR_CASE_1_R = {1, 0, 0, 1, 1}; //●○○●●

    final static int[] FOUR_CASE_2_L = {1, 1, 0, 1, 0}; //●●○●○
    final static int[] FOUR_CASE_2_R = {0, 1, 0, 1, 1}; //○●○●●

    final static int[] FOUR_CASE_3_L = {1, 1, 1, 0, 0}; //●●●○○
    final static int[] FOUR_CASE_3_R = {0, 0, 1, 1, 1}; //○○●●●

    final static int[] FOUR_CASE_4 = {1, 0, 1, 0, 1}; //●○●○●

    final static int MAX_VT_PATH = 1000;

}
