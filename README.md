# Spring을 서비스하는 Tomcat을 이중화하자 

이전 게시물에서 spring으로 게시판(miniboard)을 만들고,<br>
linux 서버에서 tomcat으로 서비스를 했다. <br>
이전 포스트 : https://github.com/hanhunh89/spring-miniBoard


이제 tomcat을 이중화 해보자. 

## mariaDB 설치
```
sudo apt install mariadb-server
```

## database 구성
```
sudo mysql -u root  // db 접속
CREATE DATABASE myDB;   // myDB database 생성
USE myDB;		// database 사용

CREATE TABLE post (      //post table 생성
    postId INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(30),
    content TEXT,
    view INT DEFAULT 0,
    writer VARCHAR(30),
    imageName VARCHAR(50)
);

CREATE TABLE user (    // user table 생성
    userId VARCHAR(50) NOT NULL PRIMARY KEY,
    password VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    auth VARCHAR(20) DEFAULT 'ROLE_USER'
);


CREATE USER 'myuser'@'11.22.33.44'' IDENTIFIED BY '1234';  //myuser 계정 생성 11.22.33.44 는 tomcat ip로 변경
GRANT ALL PRIVILEGES ON myDB.* TO 'myuser'@'11.22.33.44' WITH GRANT OPTION; //11.22.33.44는 tomcat ip로 변경

FLUSH PRIVILEGES;   // 적용
```

## db서버 원격접속 설정
설정파일을 열고
```
sudo nano /etc/mysql/mariadb.conf.d/50-server.cnf
```
다음을 주석처리한다. 
```
#bind-address= 127.0.0.1
```
db서버 재시작
```
sudo service mysql restart
```

## tomcat 서버 db접속 설정
security-context.xml을 열고
```
sudo nano /var/lib/tomcat9/webapps/miniboard/WEB-INF/spring/security-context.xml
```
db 서버 ip주소를 입력한다. 
```
<!-- old config 
<beans:property name="url" value="jdbc:mysql://localhost:3306/myDB?serverTimezone=UTC"/>
-->           
<beans:property name="url" value="jdbc:mysql://123.123.123.123:3306/myDB?serverTimezone=UTC"/>  //123.123.123.123은 db 서버 ip로 변경
```

servelt-context.xml도 열고
```
sudo nano /var/lib/tomcat9/webapps/miniboard/WEB-INF/spring/appServlet/servlet-context.xml
```
db서버 ip주소를 동일하게 입력한다. 
```
<!-- old config 
<beans:property name="url" value="jdbc:mysql://localhost:3306/myDB?serverTimezone=UTC"/>
-->           
<beans:property name="url" value="jdbc:mysql://123.123.123.123:3306/myDB?serverTimezone=UTC"/> //123.123.123.123은 db 서버 ip로 변경
```

tomcat을 재시작한다. 
```
sudo service tomcat9 restart
```

## Google Cloud Platform 방화벽 설정

compute engine -> 방화벽 규칙 설정 -> 방화벽 규칙 만들기
방화벽 규칙 만들기에서 다음과 같이 입력
```
이름 : db3306
설명 : db3306
대상 : 지정된 대상 태그
대상태그 : my_db_tag
소스 ipv4 범위 : tomcat 서버의 ip 입력
지정된 프로토콜 및 포트 선택
tcp 선택
포트 : 3306
만들기 클릭
```

compute engine -> db서버 이름 클릭 ->  수정 -> "네트워크 태그" 에 위의 "대상태그"에서 입력했던 my_db_tag 입력 -> 저장
