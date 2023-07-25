# Template
{"kor":{"title":"title","content":"content"},"eng":{"title":"title", "content":"content"}}

# 2022-8-25

## GomokuBot이 완전히 새로워 졌습니다!
2018년 6월 GomokuBot이 처음 만들어진 이래, 4년 남짓의 시간 동안 5,328개의 서버와 21,548명의 유저가 75,506판의 게임을 플레이했습니다. - 정말 감사합니다. 하지만 그와 마찬가지로 GomomkuBot은 꽤 낡았습니다. 이에 따라, 여러 강력한 기능들과 함께 GomokuBot이 완전히 새롭게 다시 만들어졌습니다!

* 처음부터 다시 만들어짐
* [렌주룰](https://www.renju.net/rules/) 지원
* 빗금 명령어 지원
* 멘션 접두사 지원
* 이미지형 보드 지원
* 대결 요청 시스템
* AI 보조 입력 시스템
* 개선된 AI 추론 엔진
* 개선된 순위 기능
* 개선된 설정 기능
* 기타 등등

/help 또는 ~help 명령어로 새롭게 추가된 명령어들을 확인해주세요. 새로 추가된 빗금 명령어를 사용하기 위해서는 ``~lang`` 명령어를 통해 언어를 다시 설정해야만 합니다.

## GomokuBot is completely new!

content":"Since the creation of GomokuBot in June 2018, 5,328 servers and 21,548 users have played 75,506 games in just over four years. — Thank you very much. But as is, GomokuBot is pretty old. Accordingly, the GomokuBot has been completely rewritten with many powerful features!

* All-new codebase
* Renju rule support
* Slash commands support
* Mention prefix support
* Image board renderer
* Match request system
* AI-Aided input system
* Enhanced AI engine
* Enhanced ranking feature
* Enhanced setting feature
* Et cetera

Check the newly added commands with /help or ~help commands. To use the newly added Slash Commands, you must reset language by ``~lang`` command.

# 2023-6-28

## GomokuBot에서 오프닝룰을 사용해 보세요.
아무런 제한사항이 없는 오목에서 흑이 항상 승리 전략을 찾아낼 수 있음은 매우 자명합니다. 그렇다면, GomokuBot이 대안으로 제시한 렌주는 공평할까요? — 안타깝지만 그렇지 않습니다. 렌주 역시 두 플레이어의 역량이 높아짐에 따라 흑이 항상 승리 전략을 찾아낼 수 있는 동전 던지기에 수렴됩니다.

그렇다면 ``공평한 오목``은 존재하지 않는 것일까요? 걱정하지 마세요. GomokuBot은 이제 새로운 대안을 제시합니다. 바로 국제 오목대회에서 사용하는 오프닝 룰입니다. 오프닝 룰은 대국 초반 흑이 매우 유리한 모양을 만드는 것을 방지하는 일련의 절차로, 두 플레이어의 협상을 통해 공평한 모양으로 게임을 시작하게 만드는 규칙입니다.

물론 여러분이 오목 초심자라면 전혀 걱정하실 필요 없습니다. 렌주마저 불공평해지는 것은, 수백 가지의 초반 경우의 수를 암기하게 되는 상당한 상급자부터입니다. GomokuBot 기본 인공지능을 언제나 확실히 이길 수 있으며, 이보다도 한참 위의 실력을 갖춘 것이 아니라면 전혀 신경 쓸 필요 없습니다.

여러분이 오목에 있어 상당한 실력자이고, 더 공평한 오목을 원한다면, 국제 오목대회에서 사용하는 오프닝룰을 GomokuBot에서 사용해 보세요. ``/start @user @rule``과같이 뒤에 사용할 룰 종류를 붙여 주기만 하면 됩니다. GomokuBot은 다음과 같은 오프닝 룰을 지원합니다.

* Soosyrv-8 오프닝 룰
* Taraguchi-10 오프닝 룰

오프닝 룰에 대해 자세히 알아보고 싶다면, [국제 오목협회(RIF)](https://www.renju.net/) 또는 GomokuBot 도움말을 참조해 주세요. 이번 업데이트로 GomokuBot에 도움말 바로가기 기능이 추가되어 쉽게 찾아보실 수 있습니다.

## Opening-rules with GomokuBot.
It's pretty clear that black can always find a winning strategy in gomoku without any restrictions, so is the alternative, renju, fair? - Unfortunately, no. Even renju leads to a coin toss where black can always find a winning strategy as the skill of both players rises.

Does this mean that there is no such thing as a ``fair gomoku``? Don't worry. GomokuBot now provides a new alternative. It's called the opening rule, which is used in international gomoku competitions. The opening rules are a set of procedures that prevent Black from creating a very advantageous position at the beginning of the game, forcing the two players to negotiate and start the game with an equivalent position.

Of course, if you're a Gomoku beginner, you don't have to worry. Even Renju only becomes unfair at a fairly advanced level, where you've memorized hundreds of initial cases. You'll always be able to beat GomokuBot's standard AI, and unless you're way above that level, you shouldn't worry about it at all.

If you're a pretty good gomoku player, and you want to play gomoku in a fair way, then try to use the opening rules that are used in international gomoku competitions in GomokuBot. All you need to do is type ``/start @user @rule`` followed by the type of rule you want to use. GomokuBot supports the following opening rules:

* Soosyrv-8 opening rule
* Taraguchi-10 opening rule

If you want to learn more about opening rules, please refer to the [Renju International Federation (RIF)](https://www.renju.net/) or the GomokuBot help. With this update, GomokuBot has added a help shortcut to make it easier to find.
