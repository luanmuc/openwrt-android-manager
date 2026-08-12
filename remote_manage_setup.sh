#!/bin/sh
# OpenWrt远程管理配置脚本
# 支持DDNS + 端口映射 + HTTPS配置
# 使用方法：sh remote_manage_setup.sh

echo "========================================="
echo "  OpenWrt 远程管理配置脚本"
echo "========================================="
echo ""

# 检查是否为root
if [ "$(id -u)" != "0" ]; then
    echo "错误：请使用root用户运行此脚本"
    exit 1
fi

# ==========================================
# 1. 配置DDNS
# ==========================================
echo "[1/4] 配置DDNS动态域名解析"
echo ""
echo "请选择DDNS服务商："
echo "  1) 花生壳 (oray.com)"
echo "  2) 阿里云 (aliyun.com)"
echo "  3) Cloudflare"
echo "  4) No-IP (noip.com)"
echo "  5) 跳过（已有DDNS）"
echo ""
read -p "请输入选项 [1-5]: " ddns_choice

case $ddns_choice in
    1)
        echo "配置花生壳DDNS..."
        read -p "请输入花生壳域名: " oray_domain
        read -p "请输入花生壳用户名: " oray_user
        read -p "请输入花生壳密码: " oray_pass
        
        # 安装ddns-scripts
        opkg update
        opkg install ddns-scripts
        
        # 配置花生壳
        uci set ddns.myddns=service
        uci set ddns.myddns.service_name="oray.com"
        uci set ddns.myddns.domain="$oray_domain"
        uci set ddns.myddns.username="$oray_user"
        uci set ddns.myddns.password="$oray_pass"
        uci set ddns.myddns.ip_source="network"
        uci set ddns.myddns.ip_network="wan"
        uci set ddns.myddns.enabled="1"
        uci commit ddns
        
        /etc/init.d/ddns restart
        echo "花生壳DDNS配置完成"
        ;;
    2)
        echo "配置阿里云DDNS..."
        read -p "请输入阿里云域名: " aliyun_domain
        read -p "请输入AccessKey ID: " aliyun_key
        read -p "请输入AccessKey Secret: " aliyun_secret
        
        opkg update
        opkg install ddns-scripts curl
        
        # 阿里云DDNS需要自定义脚本
        cat > /usr/lib/ddns/update_aliyun_com.sh << 'ALIYUN_EOF'
#!/bin/sh
# 阿里云DDNS更新脚本
DOMAIN="$1"
RECORD="$2"
ACCESS_KEY="$3"
ACCESS_SECRET="$4"

# 获取公网IP
IP=$(curl -s http://ipv4.icanhazip.com)

# 阿里云API签名（简化版）
# 实际使用需要完整的签名算法
echo "更新阿里云DNS: $RECORD.$DOMAIN -> $IP"
ALIYUN_EOF
        chmod +x /usr/lib/ddns/update_aliyun_com.sh
        
        echo "阿里云DDNS脚本已创建，请完善签名算法"
        ;;
    3)
        echo "配置Cloudflare DDNS..."
        read -p "请输入Cloudflare域名: " cf_domain
        read -p "请输入API Token: " cf_token
        read -p "请输入Zone ID: " cf_zone
        
        opkg update
        opkg install ddns-scripts curl
        
        cat > /usr/lib/ddns/update_cloudflare_com.sh << 'CF_EOF'
#!/bin/sh
# Cloudflare DDNS更新脚本
TOKEN="$1"
ZONE_ID="$2"
DOMAIN="$3"

IP=$(curl -s http://ipv4.icanhazip.com)

# 获取记录ID
RECORD_ID=$(curl -s -X GET "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/dns_records?name=$DOMAIN" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | python3 -c "import sys,json; print(json.load(sys.stdin)['result'][0]['id'])" 2>/dev/null)

# 更新记录
curl -s -X PUT "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/dns_records/$RECORD_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  --data "{\"type\":\"A\",\"name\":\"$DOMAIN\",\"content\":\"$IP\",\"ttl\":120,\"proxied\":false}"

echo "Cloudflare DDNS更新完成: $DOMAIN -> $IP"
CF_EOF
        chmod +x /usr/lib/ddns/update_cloudflare_com.sh
        echo "Cloudflare DDNS脚本已创建"
        ;;
    4)
        echo "配置No-IP DDNS..."
        read -p "请输入No-IP域名: " noip_domain
        read -p "请输入No-IP用户名: " noip_user
        read -p "请输入No-IP密码: " noip_pass
        
        opkg update
        opkg install ddns-scripts
        
        uci set ddns.myddns=service
        uci set ddns.myddns.service_name="no-ip.com"
        uci set ddns.myddns.domain="$noip_domain"
        uci set ddns.myddns.username="$noip_user"
        uci set ddns.myddns.password="$noip_pass"
        uci set ddns.myddns.ip_source="network"
        uci set ddns.myddns.ip_network="wan"
        uci set ddns.myddns.enabled="1"
        uci commit ddns
        
        /etc/init.d/ddns restart
        echo "No-IP DDNS配置完成"
        ;;
    5)
        echo "跳过DDNS配置"
        ;;
    *)
        echo "无效选项，跳过"
        ;;
esac

echo ""

# ==========================================
# 2. 配置HTTPS
# ==========================================
echo "[2/4] 配置HTTPS访问"
echo ""
read -p "是否启用HTTPS? [y/N]: " enable_https

if [ "$enable_https" = "y" ] || [ "$enable_https" = "Y" ]; then
    # 安装uhttpd-mod-tls
    opkg update
    opkg install uhttpd-mod-tls px5g-wolfssl
    
    # 生成自签名证书
    echo "生成SSL证书..."
    uci set uhttpd.main.redirect_https="1"
    uci commit uhttpd
    
    # 重启uhttpd
    /etc/init.d/uhttpd restart
    
    echo "HTTPS已启用，默认端口443"
    echo "注意：自签名证书会在浏览器中显示警告，APP已支持自签名证书"
else
    echo "跳过HTTPS配置，使用HTTP（不推荐）"
fi

echo ""

# ==========================================
# 3. 开放防火墙端口
# ==========================================
echo "[3/4] 开放防火墙端口"
echo ""
read -p "请输入远程管理端口 [443]: " remote_port
remote_port=${remote_port:-443}

# 添加防火墙规则
uci set firewall.remote_mgmt=redirect
uci set firewall.remote_mgmt.name="Remote Management"
uci set firewall.remote_mgmt.src="wan"
uci set firewall.remote_mgmt.src_dport="$remote_port"
uci set firewall.remote_mgmt.dest="lan"
uci set firewall.remote_mgmt.dest_ip="192.168.1.1"
uci set firewall.remote_mgmt.dest_port="$remote_port"
uci set firewall.remote_mgmt.proto="tcp"
uci set firewall.remote_mgmt.enabled="1"
uci commit firewall

# 重启防火墙
/etc/init.d/firewall restart

echo "防火墙端口 $remote_port 已开放"
echo ""

# ==========================================
# 4. 安全加固建议
# ==========================================
echo "[4/4] 安全加固建议"
echo ""
echo "========================================="
echo "  安全建议"
echo "========================================="
echo "1. 修改默认root密码，使用强密码"
echo "   passwd root"
echo ""
echo "2. 禁用SSH密码登录，使用密钥登录"
echo "   uci set dropbear.@dropbear[0].PasswordAuth='0'"
echo "   uci commit dropbear"
echo "   /etc/init.d/dropbear restart"
echo ""
echo "3. 限制远程管理访问IP（可选）"
echo "   uci set firewall.remote_mgmt.src_ip='你的IP'"
echo "   uci commit firewall"
echo "   /etc/init.d/firewall restart"
echo ""
echo "4. 定期更新固件和软件包"
echo "   opkg update && opkg upgrade"
echo ""
echo "5. 使用非标准端口（如8443代替443）"
echo ""
echo "========================================="
echo "  配置完成"
echo "========================================="
echo ""
echo "请在APP中配置："
echo "  - 远程地址：你的DDNS域名"
echo "  - 端口：$remote_port"
echo "  - 协议：$([ "$enable_https" = "y" ] && echo "HTTPS" || echo "HTTP")"
echo ""
echo "然后点击\"测试远程连接\"验证配置"
