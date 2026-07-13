# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

GitHubリポジトリ検索Androidアプリ。Kotlin / Jetpack Compose / Hilt / MVVM。コメント・コミットメッセージ・READMEはすべて日本語で書かれているため、それに合わせること。

## コマンド

```bash
./gradlew assembleDebug          # デバッグAPKをビルド
./gradlew test                   # JVMユニットテスト（app/src/test）
./gradlew connectedAndroidTest   # 実機/エミュレータ上の計装テスト（app/src/androidTest）
./gradlew lint                   # Android Lint
```

単一テストを実行する場合（クラス指定 / メソッド名でフィルタ）。`--tests` は集約タスク `test` では使えないので変種タスクを指定する

```bash
./gradlew :app:testDebugUnitTest --tests "com.sakurasano.reposearch.ui.<TestClass>"
./gradlew :app:testDebugUnitTest --tests "*<TestClass>.<テスト名>"
```
テスト名は日本語のバッククォート関数名（``fun `〜する`()``）。

## アーキテクチャ

Googleのアプリアーキテクチャガイドに沿った3層構成。パッケージは `com.sakurasano.reposearch` 配下で、各層の責務は次の通り（具体的なクラス構成はディレクトリを参照）

- `ui/` — Compose画面とViewModel。状態は `StateFlow` として公開し、画面はそれを受け取って表示するだけ（単方向データフロー）。UI層のコードは、Androidや通信ライブラリの型に直接依存させない。
- `data/` — Repository（interface + 実装）、RetrofitのAPI、APIレスポンスDTOとドメインへの変換。外部I/Oはこの層の中で完結させる。
- `model/` — ライブラリに依存しないドメイン型。UI層とData層の両方から参照される。
- `di/` — Hiltモジュール。ネットワーク関連のインスタンスをprovideし、interfaceに実装を `@Binds` で結びつける。

複数ファイルを横断して理解すべき設計上の約束

- エラーはデータ層で吸収する。Repository実装は通信ライブラリ/HTTPの例外を全てcatchし、`AppError`（sealed）へ変換して `DataResult<T>` で返す。UI層は通信ライブラリの例外を一切扱わない。ただし `CancellationException` だけは飲み込まず再throwする（コルーチンのキャンセルを壊さないため）。
- リポジトリの戻り値は必ず `DataResult<T>`。`Success(data)` / `Failure(AppError)` のsealed interfaceで成功・失敗を型として強制する。
- 画面の状態はsealedなUiState型に限定する。画面はその限定された状態だけを表示に反映する。一覧系では結果0件を専用の空状態で表し、`Success(emptyList())` と混同しない。
- ViewModelの取得ジョブは競合させない。ViewModelが `viewModelScope.launch` で非同期の取得を行い再実行され得る場合は、起動した `Job` を保持し、次の実行を始める前に必ず `cancel()` する。遅れて返った古い結果が新しい結果を上書きするのを防ぐため。仕様は各 `*ViewModelTest` の競合テスト。
- DTO→ドメイン変換はマッパーに集約する。API DTOは欠損に強くするためフィールドをnullableにし、ドメインへ変換する時点で `orEmpty()` 等で確定値に埋める。UIにnullableを漏らさない。
- エラー文言のマッピングはUI層の責務。`AppError` を `strings.xml` の文字列リソースへ対応づけるのはUI層で行い、ドメイン/データ層は表示文言を持たない。

## テスト方針

- ViewModelテストは `Dispatchers.Main` をテスト用ディスパッチャに差し替えるJUnitルールを使う（`viewModelScope` がMainを使うため）。`app/src/test` に既存の差し替えルールがある。
- リポジトリはinterfaceとして定義し、テストでは実装ではなくフェイクを注入する。固定結果を返すフェイクと、応答タイミングを手動制御できるフェイク（競合・キャンセル検証用）を使い分ける。Android非依存で状態遷移・キャンセル挙動を検証する。

## 依存関係とバージョン

依存はすべて `gradle/libs.versions.toml`（version catalog）で管理。`app/build.gradle.kts` では `libs.xxx` 経由で参照する。ハードコードしないこと。DIはHilt + KSP、JSONはkotlinx.serialization（Retrofitのconverter経由）。
