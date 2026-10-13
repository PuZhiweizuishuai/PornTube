#!/usr/bin/env bash
# 打包后端并只保留 Linux x86_64 平台的 native 运行库（javacv-platform 全平台瘦身）
set -euo pipefail
cd "$(dirname "$0")"

JAR="target/tiktube-1.4.0.jar"
export PYTHONIOENCODING=utf-8

mvn clean package -Dmaven.test.skip=true

python - "$JAR" <<'EOF'
import zipfile, re, os, sys

src = sys.argv[1]
tmp = src + '.tmp'
native = re.compile(r'-(windows|macosx|android|ios|linux)-[\w.]+\.jar$')
keep_suffix = '-linux-x86_64.jar'

zin = zipfile.ZipFile(src)
removed, removed_bytes, kept = 0, 0, []
with zipfile.ZipFile(tmp, 'w') as zout:
    for item in zin.infolist():
        name = item.filename
        base = name.rsplit('/', 1)[-1]
        if name.startswith('BOOT-INF/lib/') and native.search(base) and not base.endswith(keep_suffix):
            removed += 1
            removed_bytes += item.file_size
            continue
        if base.endswith(keep_suffix):
            kept.append(base)
        zi = zipfile.ZipInfo(name, date_time=item.date_time)
        zi.compress_type = item.compress_type
        zi.external_attr = item.external_attr
        zi.create_system = item.create_system
        zout.writestr(zi, zin.read(name))
zin.close()
os.replace(tmp, src)
print(f'剔除 {removed} 个非 linux-x86_64 native 库 ({removed_bytes/1024/1024:.1f} MB)')
print('保留:', ', '.join(sorted(kept)))
print(f'最终 jar: {src} ({os.path.getsize(src)/1024/1024:.0f} MB)')
EOF
