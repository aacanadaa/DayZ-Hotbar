# DayZ Hotbar

[English](README.md) · [中文](README.zh.md) · [Français](README.fr.md) · [日本語](README.ja.md) · **한국어**

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/dayz-hotbar?label=Modrinth&logo=modrinth)](https://modrinth.com/mod/dayz-hotbar)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1693963?label=CurseForge&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-hotbar)
![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%20%7C%20...%20%7C%2026.3-62b47a.svg)
![Loader](https://img.shields.io/badge/Loader-Fabric%20%7C%20Forge%20%7C%20NeoForge-dbb69b.svg)
[![Issues](https://img.shields.io/github/issues/aacanadaa/DayZ-Hotbar?color=red)](https://github.com/aacanadaa/DayZ-Hotbar/issues)
[![Ko-fi](https://img.shields.io/badge/Ko--fi-Support%20me-ff5e5b?logo=kofi&logoColor=white)](https://ko-fi.com/suoim)

Minecraft HUD를 DayZ 스타일의 핫바와 DayZ 스타일의 상태 표시로 교체하며,
[DayZ Inventory](https://github.com/aacanadaa/DayZ-Inventory)와 시각적으로 일치하도록
디자인되었습니다.

**Minecraft 1.20.1부터 26.3까지, 단일 소스 트리와 세 로더로 지원합니다. 어떤 로더도 API 모드가
필요 없습니다.**

## 지원 버전

전체 버전 매트릭스는 [Stonecutter](https://stonecutter.kikugie.dev/) 조건부 컴파일로 단일 소스
트리에서 빌드되며, (로더 × 게임 버전) 쌍마다 jar 하나를 생성합니다.

| Minecraft | Fabric | Forge | NeoForge | Java |
| :--- | :---: | :---: | :---: | :---: |
| **1.20.1 – 1.20.4** | ✅ | — | — | 17 |
| **1.20.5** | ✅ | — | — | 21 |
| **1.20.6 – 1.21.1** | ✅ | ✅ | ✅ | 21 |
| **1.21.2** | ✅ | — | ✅ | 21 |
| **1.21.3 – 1.21.5** | ✅ | ✅ | ✅ | 21 |
| **1.21.6 – 1.21.7** | ✅ | — | ✅ | 21 |
| **1.21.8 – 1.21.11** | ✅ | ✅ | ✅ | 21 |
| **26.1 – 26.3** | ✅ | — | ✅ | 25 |

Forge에는 1.21, 1.21.6, 1.21.7이 없습니다: 이 Forge 계열에는 후킹할 HUD 레이어 API가 없습니다.
Forge에는 1.21.2(미공개)와 26.x도 없습니다. 전체 판단 근거는
[docs/BUILDING.en.md](docs/BUILDING.en.md)에 기록되어 있습니다.

모든 빌드는 같은 HUD를 그리지만 jar는 **호환되지 않습니다** — 게임 버전과 로더에 맞는 것을
선택하세요.

> **팁 — GUI 배율.** HUD는 고정 픽셀 크기로 레이아웃되며 Minecraft 기본 *자동* GUI 배율에 맞춰
> 조정되어 있습니다. GUI 배율이 **크면** 핫바, 플레이어 패널, 상태 표시가 서로 밀려 화면 중앙에서
> 부딪힐 수 있습니다; **매우 작으면** 아이콘 읽기가 어려워집니다. 어느 쪽이든 발생하면
> **옵션 → 비디오 설정 → GUI 배율**을 조정하세요.

![게임 내 DayZ Hotbar HUD: 왼쪽 아래에 빈 손을 보여주는 손에 든 아이템 패널, 손에 든 슬롯이 초록으로 켜진 아홉 슬롯 핫바, 오른쪽 아래에 식량·물·온도·경험·생명을 보여주는 상태 표시](docs/screenshots/uwu.png)
![DayZ Inventory UI — Jukebox 서랍이 열린 Vicinity 격자, Survivor 패널, Decorated Pot을 보여주는 2.0x Hands 슬롯, 2x2 제작 격자](https://cdn.modrinth.com/data/8asZxzdc/images/66b7282b83c958bd63ec912c7353bb4817bc202a.png)

인벤토리 모드와 함께 사용한 모습 ^^

---

## 기능

### 핫바

오프핸드를 포함해 아홉 슬롯을 한 줄 평면으로 배치하며, DayZ Inventory 화면과 같은 거의 검은
반투명 스타일입니다. 각 회색 상자는 22x22이며 상자 사이에 픽셀 하나 간격만 있고, 받침판이나
외곽선은 어디에도 없습니다 — 슬롯 상태는 색칠의 색만으로 전달됩니다.

| 상태 | 의미 |
| :--- | :--- |
| 어두운 색칠 | 비어 있음 |
| 밝은 색칠 | 아이템 보유 |
| 녹색 색칠 | 현재 손에 든 슬롯 |
| 빨간 색칠 | 손에 들었지만 아이템이 재사용 대기 중이라 사용 불가 |

슬롯 전환에는 애니메이션이 있습니다: 새 슬롯은 **노란색**으로 시작해 약 3분의 1초에 걸쳐
**초록색**으로 안정되므로, 교체가 순간적인 전환이 아니라 사건으로 읽힙니다.

![검, 곡괭이, 스테이크 묶음, 횃불, 황금 사과 묶음이 담긴 핫바. 손에 든 스테이크 슬롯이 초록으로 켜져 있고 묶음 슬롯에는 아이템 수가 그려져 있음](docs/screenshots/hud-full-hotbar.png)

### 상태 표시

오른쪽 아래에 가로 아이콘 행이 있으며, 핫바와 같은 여백에 놓여 HUD 전체가 화면을 가로지르는 한 줄로
읽힙니다. 각 아이콘은 **용기**로 그려집니다 — 윤곽, 칸 하나의 빈 간격, 그리고 값이 오를수록 아래에서
채워지는 내부. 윤곽은 채움과 같은 색을 가지므로 노란 아이콘은 노란 윤곽을 가집니다.

이 행은 왼쪽에서 오른쪽으로 세 구역으로 나뉩니다:

| 구역 | 아이콘 |
| :--- | :--- |
| **효과** | 활성 포션 효과 종류당 하나의 표시 |
| **보급** | 식량은 사과, 포만감은 더 밝은 색칠로 그 위에; 물은 병; 온도는 온도계; 공기는 기포로, 수중일 때만 표시 |
| **활력** | 경험은 안에 레벨 숫자가 들어간 물방울; 흡수는 금색 십자; 생명은 십자, 탑승 시에는 마운트의 생명 |

구역 사이는 세로선으로 구분하며 양쪽에 내용이 있을 때만 그립니다 — 따라서 효과 구역의 구분선은 효과
자체와 함께 오갔다가 사라지고, 보급과 활력 사이는 항상 존재합니다.

흡수는 전용 아이콘 대신 **두 번째 십자**로 그려지며 오른쪽 위에 작은 더하기 배지가 붙습니다 —
더하기가 둘을 구별합니다.

오고 가는 아이콘은 항상 있는 아이콘을 밀어내지 않습니다: 이 행은 오른쪽 정렬이라 왼쪽에서 늘고
줄어듭니다.

> **물과 온도는 그리지만 아직 읽지 않습니다.** 물은 식량을 반영하므로 식량이 움직일 때 함께
> 움직입니다. 온도는 절반·흰색에 머물며, 온도계에서 "쾌적"을 뜻합니다. 둘 다 갈증과 온도
> 시스템의 자리표시자이며, 그런 mod가 있으면 실제 읽기값이 됩니다.

### 색상 구간

생명과 식량은 서로 다른 **DayZ 고유의 구간**을 사용합니다. 생명은 100 HP 기준, 식량은 5,000점
예비량 기준으로 표시합니다.

| | 흰색 | 노란색 | 빨간색 | 깜빡임 |
| :--- | :--- | :--- | :--- | :--- |
| **생명** | 61–100% | 31–60% | 15–30% | 0–14% |
| **식량** | 16–100% | 6–15% | 2–5% | 0–1.9% |

Minecraft의 막대는 둘 다 20점이라, 생명은 12 이하에서 노랗게, 6 이하에서 빨갛게, 3 미만에서
깜빡이고; 식량은 3 이하에서 노랗게, 1에서 빨갛게, 비었을 때만 깜빡입니다. 식량은 의도적으로 더
관대합니다: DayZ에서는 배고픔 경고가 출혈 경고보다 훨씬 늦게 옵니다.

![임계 수준의 상태 표시: 식량 사과, 물병, 생명 십자가 모두 빨갛게 깜빡이는 반면, 유익 효과 하트와 경험 방울은 흰색 그대로임](docs/screenshots/uwu-icons.gif)

임계 구간의 움직임 — 식량, 물, 생명이 모두 비어 있으면 빨갛게 깜빡입니다. 왼쪽의 유익 효과 하트와
경험 방울은 그동안도 흰색 그대로입니다. 효과는 켜짐/꺼짐뿐이고, 레벨 중간인 것은 경고가 아니기
때문입니다.

공기는 자체 구간이 없고 생명의 구간을 빌립니다 — 질사와 실혈은 같은 종류의 비상 상황이기 때문입니다.
흡수와 경험은 의도적으로 **색상 계층을 적용하지 않습니다**: 흡수가 적은 것은 경고가 아니며, 레벨
중간인 것도 경고가 아닙니다.

### 추세 표시

누적 셰브ロン이 각 스탯의 방향을 보여줍니다 — 오르면 아이콘 **위**, 내리면 **아래**에 놓여,
표시는 값이 향하는 쪽에 있습니다.

- **셰브론 하나** — 일반적 변동
- **셰브론 둘** — 유의미한 변화. 독 데미지나 재생 효과가 자연스러운 배고픔 감소와 다르게 읽히는
  것이 이것입니다

움직임이 멈춘 뒤에도 1.5초간 유지 후 서서히 사라집니다 — 짧은 교전이 보기 전에 지나가 버리기에
충분히 깁니다. 임계값은 스탯별로 1초 동안 측정되며, 움직임이 느린 스탯에는 의도적으로 낮게
설정됩니다: 자연 회복은 초당 약 0.25 HP뿐이라, 임계값 1.0은 절대 발동하지 않아 회복 중임을
영원히 알 수 없게 됩니다.

온도에는 표시가 절대 나타나지 않습니다. 화살표는 당신이 행동할 수 없는 방향을 가리킬 것이며,
궁극적으로 담을 읽기값은 추세가 아니라 수준이기 때문입니다.

### 플레이어 패널

왼쪽 아래 모서리에 핫바 슬롯과 같은 색칠의 두 줄 패널이 있습니다.

- **윗줄** — 손에 든 아이템: DayZ 상태 점(새것, 손상됨, 파손, 크게 파손, 부서짐)과 그 이름.
  오른쪽 절반은 무기의 발사 모드·사거리·탄약을 위해 의도적으로 비워둡니다.
- **아랫줄** — 자세 인형(걷기, 전투 달리기, 웅크림), 방패 표시, 그리고 갑옷 막대.

![왼쪽 아래의 플레이어 패널: Diamond Pickaxe 이름 옆에 새것 상태 점, 아래 줄에 걷기 자세 인형과 일부만 채워진 갑옷 막대](docs/screenshots/hud-held-tool.png)

### 효과 표시

활성 포션 효과는 효과당이 아니라 **종류당 하나의 표시**로 축소합니다 — Minecraft에는 30가지가
넘는 효과가 있고 효과마다 늘어나는 줄은 화면 가장자리를 잠식하기 때문입니다. 한눈에 중요한 것은
어떤 *종류*의 것이 걸려 있는지입니다.

| 표시 | 종류 |
| :--- | :--- |
| 하트 | 유익 — 속도, 힘, 야간 투시 |
| 알약 | 회복 — 재생, 흡수, 포만 |
| 깨진 하트 | 유해 — 독, 배고픔, 채굴 피로, 그 외 나쁜 것 전부 |

효과는 켜짐/꺼짐뿐이므로 모두 흰색으로 그리고 채움 수준이나 색상 구간이 없습니다. 효과가 끝나면
표시는 사라지지 않고 1초에 걸쳐 **서서히 사라집니다**.

![식량, 물, 온도, 경험, 생명, 흡수 아이콘 옆에 여러 포션 효과 표시가 한꺼번에 채워진 상태 표시](docs/screenshots/hud-effect-marks.png)

### 손그림, 텍스처 아님

모든 아이콘은 텍스처에서 불러오지 않고 소스의 15x15 격자에 정의된 픽셀 아트입니다. 모드는 자체
아이콘 아트를 포함하지 않아 리소스 팩과 충돌할 수 없습니다.

---

## 설치

Minecraft 버전과 로더에 맞는 jar를 선택하세요. 파일명에 둘 다 들어 있습니다, 예:
`dayz-hotbar-fabric-1.21.1-1.2.0.jar`. 모든 빌드는 같은 HUD를 그리지만 다른 버전·로더용으로
빌드되어 **호환되지 않습니다**.

1. 게임 버전에 맞는 로더를 설치 — [Fabric Loader](https://fabricmc.net/use/),
   [Forge](https://files.minecraftforge.net/net/minecraftforge/forge/) 또는
   [NeoForge](https://neoforged.net/).
2. 맞는 jar를 `mods` 폴더에 넣습니다.

다운로드는 [releases 페이지](https://github.com/aacanadaa/DayZ-Hotbar/releases)와, 실행 중인
버전 항목으로 두 스토어에 있습니다.

어떤 로더도 API 모드가 필요 없습니다 — Fabric API도, Forge/NeoForge 쪽 추가 것도 없습니다.
Forge와 NeoForge는 비슷해 보이지만 별도의 다운로드입니다: 다른 로더에 다른 HUD API를 가지고
jar는 호환되지 않습니다.

---

## 의존성

| | 요구 사항 |
| :--- | :--- |
| 모드 버전 | 1.2.0 (단일 소스가 1.20.1 – 26.3를 커버) |
| Fabric | 게임 버전에 맞는 Fabric Loader |
| Forge | HUD 레이어 API를 제공하며 게임 버전에 맞는 Forge 계열 |
| NeoForge | 1.20.6 이상 |
| Java | 1.20.5+는 21+; 1.20.1–1.20.4는 17; 26.x는 25 |
| Fabric API | 불필요 |
| Forge / NeoForge API 모드 | 불필요 |

---

## 참고

- 기본 생명·배고픔·갑옷·공기·경험 요소는 그 위에 그리지 않고 숨기므로 이중 그림이 없습니다.
- 기본 가시성 규칙이 그대로 유지됩니다: HUD는 열린 화면 뒤, 관전 모드, F1 누를 때 여전히
  숨습니다.
- 기본 공격력 표시기는 핫바 안에 있었으므로 핫바 교체로 함께 제거됩니다. 재구현하지 않습니다 —
  원하면 **옵션 → 비디오 설정 → 공격 표시기**를 *조준점*으로 설정하세요.
- **Forge 1.20.6 및 1.21.1–1.21.5에서는** 작은 기본 요소 두 개도 함께 사라집니다: 짧은
  "선택 아이템 이름" 팝업과 말 탈 때의 점프 충전 막대. 이 Forge 계열은 슬롯 줄, 경험 막대,
  생명 줄, 마운트 생명을 한 레이어에 두므로 남길 더 세분화된 것이 없습니다.
- **1.21.6부터** 기본은 경험 막대를 마운트 점프 게이지와 위치 표시 줄도 담는 공유 "맥락 막대"로
  옮겼습니다. 경험 막대 교체는 Fabric, NeoForge, (1.21.8부터) Forge에서 그 위젯 전체를
  제거합니다: DayZ 표시는 여전히 자신의 경험 아이콘을 그리지만 기본 위치·점프 막대는 다시
  그려지지 않습니다. 1.20.1–1.21.5에서는 세 로더 모두 유지합니다.

---

## 소스에서 빌드

이 트리는 [Stonecutter](https://stonecutter.kikugie.dev/)로 전체 매트릭스를 관리합니다:
**단일 소스**, `settings.gradle.kts`의 하나의 버전 목록, (로더 × 게임 버전) 쌍마다 빌드 노드
하나. 런처 JDK로 **JDK 25**가 필요합니다 — 각 게임 버전이 필요로 하는 Java 17 / 21 / 25
툴체인은 foojay 리졸버가 필요에 따라 내려받습니다.

```bash
# Build every version and loader in the matrix
JAVA_HOME=/path/to/jdk-25 ./gradlew chiseledBuild

# Build a single node
JAVA_HOME=/path/to/jdk-25 ./gradlew :fabric:1.21.1:build
JAVA_HOME=/path/to/jdk-25 ./gradlew :neoforge:26.2:build
JAVA_HOME=/path/to/jdk-25 ./gradlew :forge:1.21.11:build

# List every node
./gradlew matrix
```

산출물은 해당 노드의 빌드 디렉터리에 게임 버전을 파일명에 담아 저장됩니다:

- `fabric/versions/<mc>/build/libs/dayz-hotbar-fabric-<mc>-<version>.jar`
- `neoforge/versions/<mc>/build/libs/dayz-hotbar-neoforge-<mc>-<version>.jar`
- `forge/versions/<mc>/build/libs/dayz-hotbar-forge-<mc>-<version>.jar`

이것들이 모두 배포 가능한 산출물이며, 후처리 단계가 필요 없습니다. 같은 폴더에 `-sources.jar`도
기록되므로 수동 복사 때 올바른 파일을 고르기 주의하세요. 빌드 구성과 매트릭스 공백의 이유는
[docs/BUILDING.en.md](docs/BUILDING.en.md)를 참조하세요.

---

## 링크

- **소스**: <https://github.com/aacanadaa/DayZ-Hotbar>
- **이슈**: <https://github.com/aacanadaa/DayZ-Hotbar/issues>
- **변경 기록**: [CHANGELOG.md](CHANGELOG.md)
- **DayZ Inventory**: <https://github.com/aacanadaa/DayZ-Inventory>

---

## 라이선스 및 저작권

[Apache License 2.0](LICENSE)에 따라 라이선스됩니다.

자유롭게 사용·수정·재배포할 수 있습니다 — 모드팩, 서버, 상업적 이용 포함. 유일한 조건은
전달하는 모든 사본에 저작권 고지와 라이선스 사본이 함께 있다는 것입니다.

Copyright 2026 suoim.
