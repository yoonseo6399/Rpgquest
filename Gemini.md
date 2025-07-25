# Rpgquest 프로젝트 정보

<!--
이 문서는 Gemini가 Rpgquest 모드 개발의 맥락을 파악하고 일관성 있는 지원을 제공하기 위해 사용하는 내부 메모입니다.

**미래의 Gemini에게:**
이 파일은 이 프로젝트의 '기억'입니다. 새로운 기능 구현, 주요 아키텍처 변경, 또는 사용자와의 논의를 통해 중요한 결정이 내려졌을 때, 반드시 이 문서를 업데이트해야 합니다. 특히 하나의 핵심 기능 개발이 완료되는 시점(예: 'NPC 데이터 저장 시스템 완성', '기본 대화 시스템 구현 완료' 등)에 해당 내용을 요약하여 "프로젝트 진행 상황" 섹션에 기록해 주세요. 이렇게 하면 대화의 맥락을 잃지 않고 항상 최상의 지원을 제공할 수 있습니다.
-->

이 문서는 Gemini가 Rpgquest 모드 개발의 맥락을 파악하고 일관성 있는 지원을 제공하기 위해 사용하는 내부 메모입니다.

## 1. 프로젝트 개요

- **프로젝트명:** Rpgquest
- **목표:** Kotlin과 Fabric API를 사용하여 서버 사이드 RPG 퀘스트 모드 개발
- **주요 기술:**
  - Minecraft: 1.21.7
  - Mod Loader: Fabric
  - Language: Kotlin
  - Environment: Server-side

## 2. 핵심 아키텍처 및 설계 결정

### 데이터 영속성 (엔티티)

- **주요 방식:** Fabric의 **Data Attachment API (`AttachmentType`)** 를 사용하여 엔티티에 타입-세이프(type-safe) 커스텀 데이터를 영구적으로 저장합니다.
- **직렬화 라이브러리:** `kotlinx.serialization` (`@Serializable` 어노테이션 사용)
- **핵심 해결 과제:** `kotlinx.serialization`의 `KSerializer`를 Minecraft의 `com.mojang.serialization.Codec`으로 변환해야 합니다.
  - **해결책:** `utils/CodecUtils.kt` 파일에 `kserializerToCodec` 유틸리티 함수를 만들어 이 변환을 처리합니다. 이 함수는 모드의 데이터 처리에서 매우 중요한 역할을 합니다.
  - **적용 예시:** `quest/npc/NpcData.kt`에서 `NpcData` 객체를 저장하기 위한 `NPC_DATA_CODEC`와 `NPC_DATA_ATTACHMENT`를 정의하는 데 사용됩니다.

### 비동기 및 순차적 로직 (퀘스트/대화)

- **주요 방식:** **Kotlin Coroutines**를 사용하여 복잡한 상태 관리나 콜백 지옥 없이, 순차적이고 가독성 높은 비동기 코드를 작성합니다.
- **실행 컨텍스트:** `RpgCoroutineScope.kt`에서 모드의 생명주기와 함께하는 커스텀 `CoroutineScope`를 정의합니다.
  - 이 스코프는 서버 시작 시 **메인 서버 스레드**를 기본 `Dispatcher`로 사용하도록 초기화됩니다.
  - 이를 통해 `ModCoroutineScope.launch { ... }` 블록 내의 코드는 별도 설정 없이 Minecraft API를 안전하게 호출할 수 있습니다.
- **이벤트 브릿지:** `suspendCancellableCoroutine`을 사용하여 플레이어의 행동 대기, 특정 지역 도달 대기 등 이벤트 기반 로직을 코루틴의 `suspend` 함수로 변환합니다.

### NPC 시스템

- **NPC 식별:** 엔티티에 `NPC_DATA_ATTACHMENT`가 붙어있는지 여부로 NPC를 식별합니다 (`entity.isNpc()`).
- **NPC 데이터:** `NpcData` 데이터 클래스가 NPC의 정보(예: `dialogueId`)를 정의합니다.
- **대화 시스템 (계획):** `DialogueScript`와 같은 클래스를 만들어, `script.npc("...")`, `script.player("...")` 등의 `suspend` 함수를 순차적으로 호출하여 대화 흐름을 제어하는 방식으로 구현할 예정입니다.

## 3. 주요 파일 위치

- **`Rpgquest.kt`**: 모드의 메인 초기화 클래스.
- **`utils/CodecUtils.kt`**: `KSerializer`를 `Codec`으로 변환하는 핵심 유틸리티.
- **`RpgCoroutineScope.kt`**: 모드 전역에서 사용되는 메인 스레드 기반 코루틴 스코프.
- **`quest/npc/NpcData.kt`**: NPC 데이터 구조와 `AttachmentType`을 정의하는 파일.

## 4. 프로젝트 진행 상황

- **[완료]** 코루틴 기반 비동기 처리 환경 설정 (`RpgCoroutineScope`)
- **[완료]** `kotlinx.serialization`과 Minecraft `Codec`을 연동하는 데이터 영속성 기반 마련 (`CodecUtils`, `NpcData`)
