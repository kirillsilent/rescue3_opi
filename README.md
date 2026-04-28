**Daemon service file**

For starting rescue like a daemon service systemd
create file in directory /etc/systemd/system sowa.service

Example file body is:

```
[Unit]
Description=SowaApp
After=net_tools.service

[Service]
User=pi
ExecStart=/usr/bin/java -jar /home/pi/rescue/rescue-0.1.jar
restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

# rescue3_opi
