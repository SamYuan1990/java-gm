#/bin/bash
TAGPATTERN="refs/heads/v.*"
if [[ "$2" =~ $TAGPATTERN ]]; then
  gradle publish -Dtoken=$1
else
	echo "skip as not relase tag!"
fi