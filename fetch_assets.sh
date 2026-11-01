gh release download Winlator-7.1.3x-Cmod-GLIBC-v11fix1 -R coffincolors/winlator -p "*.apk" -D /tmp/cm
unzip -q -o "$(ls /tmp/cm/*.apk | head -1)" "assets/*" -d app/src/main
