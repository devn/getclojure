#!/bin/sh
SHA="a4b12e969305"

echo "Fetching Pygments..."
curl https://bitbucket.org/birkenfeld/pygments-main/get/$SHA.tar.gz -o pygments.tar.gz
tar -xvzf pygments.tar.gz
mv birkenfeld-pygments-main-$SHA resources/pygments
rm pygments.tar.gz
