# 运行这个脚本的前提:
# 1.需要先确保能正常构建ALXR客户端(参考官网或:https://iwiki.woa.com/pages/viewpage.action?pageId=4007072608)
# 2.需要手动安装cargo-ndk: cargo install cargo-ndk
set -e

rm -rf alxr_lib
mkdir alxr_lib

# 编译原始alxr客户端
mkdir -p alxr_lib/official/arm64-v8a
cargo xtask build-alxr-android --oculus-quest
cp ./target/quest/debug/apk/lib/arm64-v8a/*.so alxr_lib/official/arm64-v8a/

# 编译定制版alxr客户端
mkdir -p alxr_lib/tcr/arm64-v8a
cargo xtask build-tcr-version
cp target/quest/arm64-v8a/*.so alxr_lib/tcr/arm64-v8a/
