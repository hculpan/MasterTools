#!/bin/bash
echo "Creating icon"

if [ -d "MasterTools.iconset" ]; then
    rm -rf MasterTools.iconset
fi
if [ -f "MasterTools.icns" ]; then
    rm MasterTools.icns
fi

mkdir MasterTools.iconset
sips -z 16 16 MasterTools.png --out MasterTools.iconset/icon_16x16@2x.png
sips -z 32 32 MasterTools.png --out MasterTools.iconset/icon_32x32.png
sips -z 64 64 MasterTools.png --out MasterTools.iconset/icon_32x32@2x.png
sips -z 64 64 MasterTools.png --out MasterTools.iconset/icon_64x64.png
#sips -z 128 128 MasterTools.png --out icon_64x64@2x.png
#sips -z 128 128 MasterTools.png --out icon_128x128.png
sips -z 256 256 MasterTools.png --out MasterTools.iconset/icon_128x128@2x.png
sips -z 256 256 MasterTools.png --out MasterTools.iconset/icon_256x256.png
sips -z 512 512 MasterTools.png --out MasterTools.iconset/icon_256x256@2x.png
sips -z 512 512 MasterTools.png --out MasterTools.iconset/icon_512x512.png
sips -z 1024 1024 MasterTools.png --out MasterTools.iconset/icon_512x512@2x.png
sips -z 1024 1024 MasterTools.png --out MasterTools.iconset/icon_1024x1024.png

iconutil -c icns MasterTools.iconset

echo "Done creating MasterTools.icns"