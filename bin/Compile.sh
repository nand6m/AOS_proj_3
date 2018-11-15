cd $(dirname $0)
javac -cp .:../lib/* *.java
mv *.class ../bin -f

