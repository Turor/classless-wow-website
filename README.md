# Classless Wow Web Server
This web server is designed to support basic account creation and recovery for a classless server.
Account recovery is facilitated by manually relaying a generated token to the user.
The server generates presigned s3 urls to support a companion launcher which uses bucket keys to presigned urls for resources.
The server allows users to download a game launcher

## Implementation Notes
I've deployed this webserver on a debian 13 distribution and set it up as a systemd service. Here's the commands I use quite a bit, mostly as personal notes.

/usr/games/wow/classless-wow-website
journalctl -u classless-web-server -f
systemctl restart classless-wow-webserver.service
./gradlew build bootJar
cp ./build/libs/classless-wow-webserver-0.0.1-SNAPSHOT.jar /opt/classless-web-server/app.jar
nano /opt/classless-web-server/application.yml

scp registration@registration-dev.turoran.com:/var/lib/classless-web-server/recovery.db D:\CustomWowRebuild\recovery.db