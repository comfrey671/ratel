package org.nico.ratel.landlords.client;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nico.noson.Noson;
import org.nico.noson.entity.NoType;
import org.nico.ratel.landlords.client.entity.User;
import org.nico.ratel.landlords.client.proxy.ProtobufProxy;
import org.nico.ratel.landlords.client.proxy.WebsocketProxy;
import org.nico.ratel.landlords.features.Features;
import org.nico.ratel.landlords.print.SimplePrinter;
import org.nico.ratel.landlords.print.SimpleWriter;
import org.nico.ratel.landlords.utils.StreamUtils;

public class SimpleClient {

	public static int id = -1;

	public final static String VERSION = Features.VERSION_1_4_0;

	public static String serverAddress;

	public static int port = 1024;

	public static String protocol = "pb";

	public final static Pattern PATTERN = Pattern.compile("^(([a-zA-Z0-9._-]+)|([0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}))(:([0-9]{1,4}))$");

	private final static String[] serverAddressSource = new String[]{
			"https://raw.githubusercontent.com/ainilili/ratel/master/serverlist.json",			//Source
			"https://cdn.jsdelivr.net/gh/ainilili/ratel@master/serverlist.json",				//CN CDN
			"https://raw.fastgit.org/ainilili/ratel/master/serverlist.json",				//HongKong CDN
			"https://cdn.staticaly.com/gh/ainilili/ratel/master/serverlist.json",				//Japanese CDN
			"https://ghproxy.com/https://raw.githubusercontent.com/ainilili/ratel/master/serverlist.json",	//Korea CDN
			"https://gitee.com/ainilili/ratel/raw/master/serverlist.json"					//CN Gitee
	};

	public static void main(String[] args) throws InterruptedException, IOException, URISyntaxException {
		if (args != null && args.length > 0) {
			for (int index = 0; index < args.length; index = index + 2) {
				if (index + 1 < args.length) {
					if (args[index].equalsIgnoreCase("-p") || args[index].equalsIgnoreCase("-port")) {
						port = Integer.parseInt(args[index + 1]);
					}
					if (args[index].equalsIgnoreCase("-h") || args[index].equalsIgnoreCase("-host")) {
						serverAddress = args[index + 1];
					}
					if (args[index].equalsIgnoreCase("-ptl") || args[index].equalsIgnoreCase("-protocol")) {
						protocol = args[index + 1];
					}
				}
			}
		}
		SimplePrinter.printNotice("");

		if (serverAddress == null) {
			List<String> serverAddressList = getServerAddressList();
			if (serverAddressList == null || serverAddressList.size() == 0) {
				throw new RuntimeException("Please use '-host' to setting server address.");
			}
			SimplePrinter.printNotice("Please select a server:");
			SimplePrinter.printNotice("0. Enter the service address manually[" + VERSION + "], such as(127.0.0.1:1024)");
			for (int i = 0; i < serverAddressList.size(); i++) {
				SimplePrinter.printNotice((i + 1) + ". " + serverAddressList.get(i));
			}
			int serverPick = Integer.parseInt(SimpleWriter.write(User.INSTANCE.getNickname(), "option"));
			while (serverPick < 0 || serverPick > serverAddressList.size()) {
				try {
					SimplePrinter.printNotice("The server address does not exist!");
					serverPick = Integer.parseInt(SimpleWriter.write(User.INSTANCE.getNickname(), "option"));
				} catch (NumberFormatException ignore) {}
			}
			if(serverPick == 0){
				//手动输入服务器
				String[] address = getValidAddress(SimpleWriter.write(User.INSTANCE.getNickname(), "option"));
				while(address == null || address.length < 2){
					SimplePrinter.printNotice("Please enter a valid service address, such as(127.0.0.1:1024)");
					address = getValidAddress(SimpleWriter.write(User.INSTANCE.getNickname(), "option"));
				}
				serverAddress = address[0];
				port = Integer.parseInt(address[1]);
			}else{
				//从服务器列表选择服务器
				serverAddress = serverAddressList.get(serverPick - 1);
				String[] elements = serverAddress.split(":");
				serverAddress = elements[0];
				port = Integer.parseInt(elements[1]);
			}
		}

		if (Objects.equals(protocol, "pb")) {
			new ProtobufProxy().connect(serverAddress, port);
		} else if (Objects.equals(protocol, "ws")) {
			new WebsocketProxy().connect(serverAddress, port + 1);
		} else {
			throw new UnsupportedOperationException("Unsupported protocol " + protocol);
		}
	}

	private static String[] getValidAddress(String address){
		if(address.isEmpty()){
			return null;
		}
		Matcher matcher = PATTERN.matcher(address);
		if(matcher.find()){
			String[] res = new String[2];
			res[0] = matcher.group(1);
			res[1] = matcher.group(5);
			return res;
		}
		return null;
	}

	private static List<String> getServerAddressList() {
		for (String serverAddressSource : serverAddressSource) {
			try {
				String serverInfo = StreamUtils.convertToString(new URL(serverAddressSource));
				return Noson.convert(serverInfo, new NoType<List<String>>() {});
			} catch (IOException e) {
				SimplePrinter.printNotice("Try connected " + serverAddressSource + " failed: " + e.getMessage());
			}
		}
		return null;
	}

}
