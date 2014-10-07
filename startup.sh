#! /bin/bash 
[ -z $BAG_HOME ] && BAG_HOME=`cd ..;pwd`
echo "BAG_HOME : "$BAG_HOME

BAG_LOG=$BAG_HOME/logs
BAG_OUT=$BAG_LOG/bag.out

if [ ! -d "$BAG_LOG" ]; then
  mkdir $BAG_LOG
fi

touch "$BAG_OUT"

cd ..
java -cp .:lib/*:conf http.startup.JettyServer >> "$BAG_OUT" 2>&1 &
