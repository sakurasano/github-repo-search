# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

GitHub リポジトリ検索 Android アプリ。Kotlin / Jetpack Compose / Hilt / MVVM。コメント・コミットメッセージ・README はすべて日本語で書かれているため、それに合わせること。

## コマンド

```bash
./gradlew assembleDebug          # デバッグ APK をビルド
./gradlew test                   # JVM ユニットテスト（app/src/test）
./gradlew connectedAndroidTest   # 実機/エミュレータ上の計装テスト（app/src/androidTest）
./gradlew lint                   # Android Lint
```

単一テストを実行する場合

```bash
./gradlew test --tests "com.sakurasano.reposearch.ui.RepoSearchViewModelTest"
./gradlew test --tests "*RepoSearchViewModelTest.空白のクエリでは検索せずIdleのままになる"
```
テスト名は日本語のバッククォート関数名（``fun `検索が成功するとSuccessになる`()``）。

## アーキテクチャ

Google のアプリアーキテクチャガイドに沿った 3 層構成。パッケージは `com.sakurasano.reposearch` 配下で、各層の責務は次の通り（具体的なクラス構成はディレクトリを参照）

- `ui/` — Compose 画面と ViewModel。状態は `StateFlow` として公開し、画面はそれを受け取って表示するだけ（単方向データフロー）。UI 層のコードは、Android や通信ライブラリの型に直接依存させない。
- `data/` — Repository（interface + 実装）、Retrofit の API、API レスポンス DTO とドメインへの変換。外部 I/O はこの層の中で完結させる。
- `model/` — ライブラリに依存しないドメイン型。UI 層と Data 層の両方から参照される。
- `di/` — Hilt モジュール。ネットワーク関連のインスタンスを provide し、interface に実装を `@Binds` で結びつける。

複数ファイルを横断して理解すべき設計上の約束（クラス名はアンカーとして記載。リネーム時のみ追随すればよい）

- **エラーはデータ層で吸収する。** `RepoSearchRepositoryImpl` が Retrofit の例外を全て catch し、`AppError`（sealed）に変換して `DataResult<T>` で返す。UI 層は Retrofit/HTTP の例外を一切扱わない。`CancellationException` だけは飲み込まず再 throw する（コルーチンのキャンセルを壊さないため）。通信手段を差し替えても UI 層に影響しない。
- **`DataResult<T>`** = `Success(data)` / `Failure(AppError)` の sealed interface。リポジトリの戻り値は常にこれ。
- **`RepoSearchUiState`** = `Idle`/`Loading`/`Empty`/`Success`/`Error` の sealed interface。画面はこの限定された状態だけを表示に反映する。0 件は `Empty` で `Success(emptyList())` とは区別する。
- **検索の競合対策。** `RepoSearchViewModel.search()` は新しい検索開始時に前回の `searchJob` を cancel し、遅れて返った古い結果が新しい結果を上書きしないようにする（`RepoSearchViewModelTest` の「連続検索」テストが仕様）。
- **DTO→ドメイン変換は `RepoMapper.toDomain()`。** `RepoDto` は API の欠損に強くするため多くのフィールドが nullable で、変換時に `orEmpty()` で埋める。UI に nullable を漏らさない。
- **エラー文言のマッピングは UI 層。** `AppError.messageRes()`（`ui/AppErrorUi.kt`）が `AppError` を `strings.xml` の文字列リソースへ対応づける。

## テスト方針

- ViewModel テストは `MainDispatcherRule`（`app/src/test`）で `Dispatchers.Main` をテスト用ディスパッチャに差し替える（`viewModelScope` が Main を使うため）。
- リポジトリは interface なので、テストでは `FakeRepoSearchRepository`（固定結果）や `GatedFakeRepository`（応答タイミングを手動制御）を注入する。Android 非依存で状態遷移・キャンセル挙動を検証する。

## 依存関係とバージョン

依存はすべて `gradle/libs.versions.toml`（version catalog）で管理。`app/build.gradle.kts` では `libs.xxx` 経由で参照する。ハードコードしないこと。DI は Hilt + KSP、JSON は kotlinx.serialization（Retrofit の converter 経由）。

## 実装状況の注意

README は完成形（お気に入り/Room/Paging/詳細画面・共有/検索履歴/Navigation）を記述しているが、現時点で実装済みなのはキーワード検索と一覧表示のみ。README にある機能の多くは未実装なので、既存前提で参照しないこと。
