set -e
currentVersionCode=$1
currentVersionName=$2
function sedi {
  if [ "$(uname)" == "Linux" ]; then
    sed -i "$@"
  else
    sed -i "" "$@"
  fi
}

cd "app"


for entry in `awk '/versionCode/ {print $2}' ./build.gradle`; do
    index=`echo ${entry}`
    sedi 's/versionCode [0-9a-zA-Z -_]*/versionCode '$(($currentVersionCode + 1))'/' ./build.gradle
done

echo "Current versionName is: $currentVersionName"

echo "Setting new versionName..."

sedi 's/versionName [0-9a-zA-Z -_]*/versionName "'"$currentVersionName"'"/' ./build.gradle

sed -i 's/<string name=\"app_name\">.*<\/string>/<string name=\"app_name\">Bida app scoreboard RELEASE '$currentVersionName'<\/string>/g' src/main/res/values/strings.xml

echo "New versionName is: $currentVersionName"
