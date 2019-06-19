rmdir /s/q "../src/main/java/com/chatserver/demo/protocols"

flatc --java -o ../src/main/java/ ../src/main/resources-common/protocols-spec/%1/request.fbs
