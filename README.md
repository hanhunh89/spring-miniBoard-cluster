# Spring을 서비스하는 Tomcat을 이중화하자 

이전 게시물에서 spring으로 게시판(miniboard)을 만들고,<br>
linux 서버에서 tomcat으로 서비스를 했다. <br>
이전 포스트 : https://github.com/hanhunh89/spring-miniBoard

apache, tomcat, db 서버를 분리하고 tomcat을 이중화 해보자. 

# tomcat 서버를 하나 더 구성
이전 포스트 : https://github.com/hanhunh89/spring-miniBoard 를 참고하여 tomcat server를 구성 후,
위의 절차를 반복합니다. 

## git 설치
```
sudo apt update
sudo apt install git
```

## tomcat 설치
```
sudo apt-get update
sudo apt-get install tomcat9
sudo systemctl enable tomcat9
sudo systemctl start tomcat9
```

## war 파일 배포
```
cd /var/lib/tomcat9/webapps
sudo git clone https://github.com/hanhunh89/spring-miniBoard ./my
cd my
sudo mv ./miniboard.war ../miniboard.war
cd ..
sudo rm -rf my
```

## tomcat 재시작
```
sudo service tomcat9 restart
```

# DB 구성
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


CREATE USER 'myuser'@'11.22.33.44' IDENTIFIED BY '1234';  //myuser 계정 생성 11.22.33.44 는 tomcat1 ip로 변경
GRANT ALL PRIVILEGES ON myDB.* TO 'myuser'@'11.22.33.44' WITH GRANT OPTION; //11.22.33.44는 tomcat1 ip로 변경
CREATE USER 'myuser'@'55.55.55.55' IDENTIFIED BY '1234';  //myuser 계정 생성 55.55.55.55 는 tomcat2 ip로 변경
GRANT ALL PRIVILEGES ON myDB.* TO 'myuser'@'55.55.55.55' WITH GRANT OPTION; //55.55.55.55는 tomcat2 ip로 변경

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

## 두개의 tomcat 서버 모두에 db접속 설정을 한다
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

# Google Cloud Platform 방화벽 설정
## 방화벽 규칙 생성
compute engine -> 방화벽 규칙 설정 -> 방화벽 규칙 만들기
방화벽 규칙 만들기에서 다음과 같이 입력
```
이름 : db3306
설명 : db3306
대상 : 지정된 대상 태그
대상태그 : my_db_tag
소스 ipv4 범위 : tomcat1, 2 서버의 ip 입력
지정된 프로토콜 및 포트 선택
tcp 선택
포트 : 3306
만들기 클릭
```

compute engine -> db서버 이름 클릭 ->  수정 -> "네트워크 태그" 에 위의 "대상태그"에서 입력했던 my_db_tag 입력 -> 저장




# apache 구성을 통한 load balancing
이제 우리는 톰캣서버 두개와 db서버 하나를 갖게 되었습니다.
하지만 우리 홈페이지의 사용자는 하나의 아이피로만 접속해야 합니다.
사용자가 아파치로 접속하면 아파치가 알아서 두개의 톰캣 서버 중 하나를 골라주게 만들겠습니다.

google cloud platform에서 서버 하나를 더 생성합니다. 

## 아파치 설치
```
sudo apt-get update
sudo apt-get install apache2  
```

## reverse proxy 및 load balancing 설정
```
cd /etc/apache2/sites-available
sudo nano vi tomcat.conf
```
tomcat.conf 파일에 아래 입력
```
<VirtualHost *:80>
  ServerName 123.123.123.123 // apache 서버 ip 입력

  ProxyPass / balancer://mycluster/
  ProxyPassReverse / balancer://mycluster/

  Header add Set-Cookie "ROUTEID=.%{BALANCER_WORKER_ROUTE}e; path=/" env=BALANCER_ROUTE_CHANGED

  <Proxy balancer://mycluster>
    BalancerMember http://11.11.11.11:8080 route=1  // 11.11.11.11을 tomcat server1 ip로 변경
    BalancerMember http://22.22.22.22:8080 route=2  // 22.22.22.22를 tomcat server2 ip로 변
    ProxySet stickysession=ROUTEID
  </Proxy>  
     
  ErrorLog ${APACHE_LOG_DIR}/error.log
  CustomLog ${APACHE_LOG_DIR}/access.log combined
</VirtualHost>
```

## 필요한 mod enable
```
sudo a2enmod proxy
sudo a2enmod proxy_balancer
sudo a2enmod lbmethod_byrequests
sudo a2enmod headers
sudo a2enmod proxy_http
```

## 위에서 만든 tomcat.conf 적용
```
sudo a2ensite tomcat.conf
```

## 아파치 서버 재시작
```
sudo service apache2 restart
```


# 거의 다 되엇다. 하지만...
거의 다 되었다. 하지만 아직 문제가 있다.<br>
첨부파일인 image 파일이 각각의 tomcat 서버에 저장되어 <br>
tomcat1 에 접속하여 게시글을 작성하면, <br>
tomcat2 에 접속했을 때는 이미지가 보이지 않는다. <br>

# google Cloud Storage를 이용해보자.
google cloud storage에 이미지 파일을 저장해보자

## cloud storage 생성
```
cloud storage - 버킷 - 만들기
이름은 적히 입력하고
"객체 액세스를 제어하는 방식 선택"의 체크를 해제한다.
만들기 클릭
```

## storage upload 권한 설정
먼저 편집자 권한을 설정합니다. 이 권한으로 파일을 업로드 할 수 있습니다.  
```
IAM 및 관리자 - 서비스계정 - 서비스 계정 만들기
계정이름 입력 - 만들고 계속하기 - 역할선택에서 "편집자" 입력 후 선택
완료 클릭
이후 생성된 계정 클릭 - 키 - 키 추가 - 새 키 만들기 - 만들기
```
그러면 json 파일이 다운로드됩니다. 이후 이 파일을 spring에 등록하겠습니다. 


그 다음 사용자 권한을 생성합니다. 이 권한으로 파일을 읽을 수 있습니다. 
```
cloud storage - 버킷 - 버킷이름 클릭 - 권한 - 엑세스 권한 부여 - 새 주 구성원에 "allUsers" 입력 - 역할선택에 "저장소 객체 뷰어" 입력
저장
```
## PostDetail.jsp 수정
이미지를 불러오는 url 을 수정하자
PostDetail.jsp를 연다. 
```
sudo nano  miniboard/WEB-INF/views/PostDetail.jsp
```

url 을 수정한다.
```
<!--
<img src="<c:url value="/resources/images/${post.getImageName()}"/>" style="width:60%"/>
-->
<img src="<c:url value="http://storage.googleapis.com/miniboard-storage/${post.getImageName()}"/>" style="width:60%"/>
```
여기서 miniboard-storage는 google cloud storage에서 생성한 bucket의 이름이다. 

## google cloud storage 접속 설정
pom.xml 설정
```
<!--  google cloud storage-->
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>google-cloud-storage</artifactId>
    <version>2.22.2</version>
</dependency>

<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
    <version>31.1-jre</version>
</dependency>
```

cloud storage에 파일을 넣을 수 있는 class
```
package com.springmvc.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

public class CloudStorageUtil {

  private static String projectId = "rosy-drake-400902";
  private static String bucketName = "miniboard-storage";
  

  // upload file to GCS
  public static void uploadFile(String filePath, String file_name, String credentialsPath) throws IOException {
    GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath))
            .createScoped("https://www.googleapis.com/auth/cloud-platform");
    // storage 객체 생성
    Storage storage = StorageOptions.newBuilder().setProjectId(projectId).setCredentials(credentials).build().getService();
    // blobid 생성
    BlobId blobId = BlobId.of(bucketName, file_name);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
    //파일 업로드
    storage.createFrom(blobInfo, Paths.get(filePath+file_name));
  }
}

```
filePath = cloud storage에 올리고 싶은 파일이 위치한 디렉토리<br>
file_name = cloud storage에 올리고 싶은 파일 이름<br>
credentialPath = key 파일의 경로(디렉토리+파일이름 까지)<br>

다 되었다.. 
이제 tomcat을 이중화해서 사용할 수 있다. 
하지만 여기에도 문제가 있다. 

sticky session... 이것은 언젠가 다루어 보겟다. 
