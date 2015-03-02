package com.wily.fieldext.epaplugins.apmcloudmonitor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wily.fieldext.epaplugins.utils.CryptoUtils;
import com.wily.fieldext.epaplugins.utils.EPAConstants;
import com.wily.fieldext.epaplugins.utils.PropertiesUtils;
import com.wily.fieldext.epaplugins.utils.RESTClient;
import com.wily.fieldext.epaplugins.utils.StringFilter;
import com.wily.fieldext.epaplugins.utils.TextNormalizer;



public class APMCMReader
{
	private PropertiesUtils apmcmProperties;
	private CryptoUtils apmcmCrypto = new CryptoUtils("1D0NTF33LT4RDY");
	private RESTClient apmcmClient;
	private HashMap<String, String> cpMap;
	private HashMap<String, String[]> folderMap;
	private HashMap<String, String> creditsMap = new HashMap<String, String>();
	private String[] apmcmFolders;
	private String nkey = null;
	private String todays_date = null;
	private String apmcmUser;
	private String apmcmPass;
	public int apmcmEPAWaitTime;
	private Boolean apmcmLocalTest;
	private Boolean keepRunning;
	private Boolean apmcmDisplayMonitor;
	private int numRetriesLeft;
	private String apmcmLocalTestPath = "";
	private PrintStream ps;

	public static void main(String[] args, PrintStream psEPA)
			throws Exception
	{
		APMCMReader thisReader = new APMCMReader(args);

		thisReader.ps = psEPA;

		for (int i = 0; i < thisReader.apmcmFolders.length; i++) {
			APMCMReaderThread rt = new APMCMReaderThread(thisReader, thisReader.apmcmFolders[i]);
			rt.start();
		}

		while (thisReader.keepRunning.booleanValue())
			try
		{
				if (thisReader.apmcmProperties.getProperty("apmcm.metrics.credits", "false").equals("true")) {
					thisReader.creditsMap.putAll(thisReader.getCredits());
					thisReader.printMetrics(thisReader.creditsMap);
					thisReader.creditsMap.putAll(thisReader.resetMetrics(thisReader.creditsMap));
				}
				Thread.sleep(thisReader.apmcmEPAWaitTime);
		} catch (Exception e) {
			if ((e.toString().matches(".*(BindException|ConnectException|HttpRetryException|NoRouteToHostException|ProtocolException|SocketException|SocketTimeoutException|UnknownHostException).*")) && 
					(thisReader.numRetriesLeft > 0)) {
				thisReader.numRetriesLeft = thisReader.retryConnection(thisReader.numRetriesLeft, "Parent Thread");
			} else {
				System.err.println("Error running APM Cloud Monitor Agent - Parent Thread");
				e.printStackTrace();
				thisReader.keepRunning = Boolean.valueOf(false);
				System.exit(2);
			}
		}
	}

	public APMCMReader(String[] args)
	{
		this.keepRunning = Boolean.valueOf(true);
		this.numRetriesLeft = 10;
		try
		{
			if (args.length != 0)
				this.apmcmProperties = new PropertiesUtils(args[0]);
			else {
				this.apmcmProperties = new PropertiesUtils("APMCloudMonitor.properties");
			}

			this.apmcmEPAWaitTime = Integer.parseInt(this.apmcmProperties.getProperty("apmcm.waittime"));
			this.apmcmLocalTest = Boolean.valueOf(Boolean.parseBoolean(this.apmcmProperties.getProperty("apmcm.localtest", "false")));
			if (this.apmcmLocalTest.booleanValue()) {
				this.apmcmLocalTestPath = this.apmcmProperties.getProperty("apmcm.localtestpath");
			}


			this.apmcmUser = this.apmcmProperties.getProperty("apmcm.user");

			if (this.apmcmProperties.getProperty("apmcm.pass.encrypted").equals("true"))
				this.apmcmPass = this.apmcmCrypto.decrypt(this.apmcmProperties.getProperty("apmcm.pass"));
			else {
				this.apmcmPass = this.apmcmProperties.getProperty("apmcm.pass");
			}
			this.todays_date = getTodaysDate();

			String proxyHost = this.apmcmProperties.getProperty("apmcm.proxy.host", "");
			String proxyPort = this.apmcmProperties.getProperty("apmcm.proxy.port", "");
			String proxyUser = this.apmcmProperties.getProperty("apmcm.proxy.user", "");
			String proxyPass;
			if (this.apmcmProperties.getProperty("apmcm.proxy.pass.encrypted", "false").equals("true"))
				proxyPass = this.apmcmCrypto.decrypt(this.apmcmProperties.getProperty("apmcm.proxy.pass", ""));
			else {
				proxyPass = this.apmcmProperties.getProperty("apmcm.proxy.pass", "");
			}

			this.apmcmDisplayMonitor = Boolean.valueOf(Boolean.parseBoolean(this.apmcmProperties.getProperty("apmcm.displaymonitor", "true")));

			this.apmcmClient = new RESTClient(proxyHost, proxyPort, proxyUser, proxyPass);
		}
		catch (Exception e)
		{
			System.err.println("Error initializing Watchmouse EPAgent.");
			e.printStackTrace();
			System.exit(1);
		}

		Boolean keepTrying = Boolean.valueOf(true);
		int initNumRetriesLeft = 10;

		while (keepTrying.booleanValue())
			try {
				this.nkey = login();
				this.folderMap = getFoldersAndRules(this.apmcmProperties);
				this.cpMap = getCheckpoints(this.apmcmProperties);
				keepTrying = Boolean.valueOf(false);
			}
		catch (Exception e) {
			if ((e.toString().matches(".*(BindException|ConnectException|HttpRetryException|NoRouteToHostException|ProtocolException|SocketException|SocketTimeoutException|UnknownHostException).*")) && 
					(initNumRetriesLeft > 0)) {
				initNumRetriesLeft = retryConnection(initNumRetriesLeft, "Agent Initialization");
			} else {
				System.err.println("Error initializing Watchmouse EPAgent.");
				e.printStackTrace();
				keepTrying = Boolean.valueOf(false);
				System.exit(1);
			}
		}
	}

	private String[] compareList(List<String> masterList, String comparisonString)
	{
		List<String> checkList = Arrays.asList(comparisonString.split(","));
		masterList.retainAll(checkList);
		return (String[])masterList.toArray(EPAConstants.kNoStringArrayProperties);
	}

	private String executeAPI(String callType, String callParams) throws Exception
	{
		String apiResponse = "";
		if (!this.apmcmLocalTest.booleanValue()) {
			URL apiURL = new URL(this.apmcmProperties.getProperty("apmcm.URL") + "/" + callType);
			apiResponse = this.apmcmClient.request(EPAConstants.apmcmQuiet.booleanValue(), "POST", apiURL, callParams);
		} else if (!callType.equals("acct_logout")) {
			String inputLine = null;
			String inputFileName = this.apmcmLocalTestPath + "\\" + callType + ".txt";
			BufferedReader inputFile = new BufferedReader(new FileReader(inputFileName));
			while ((inputLine = inputFile.readLine()) != null) apiResponse = apiResponse + inputLine;
			inputFile.close();
		} else {
			return "Logged Out.";
		}

		return apiResponse.trim();
	}

	private JSONArray extractJSONArray(String metricInput, String arrayName) throws Exception
	{
		JSONObject entireJO = new JSONObject(unpadJSON(metricInput));
		JSONArray thisJA = new JSONArray();

		if (entireJO.optJSONObject("result") != null) {
			JSONObject resultJO = entireJO.getJSONObject("result");

			if (resultJO.optJSONArray(arrayName) != null) {
				thisJA = resultJO.optJSONArray(arrayName);
			}
		}

		return thisJA;
	}

	private String fixMetric(String rawMetric)
	{
		StringFilter thisNormalizer = null;
		try
		{
			thisNormalizer = TextNormalizer.getNormalizationStringFilter();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1001);
		}

		String metricKeyNormalized = thisNormalizer.filter(rawMetric);
		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		String fixedMetric = pattern.matcher(metricKeyNormalized).replaceAll("");

		return fixedMetric.replace("\\", "-").replace("/", "-").replace(",", "_").replace(";", "-").replace("&", "and");
	}

	//TODO: GG make private again
	protected HashMap<String, String> generateMetrics(String jsonString, String metricTree) throws Exception
	{
		HashMap<String, String> metric_map = new HashMap<String, String>();

		JSONObject thisJO = new JSONObject(jsonString);

		if (thisJO.optString("name", null) != null) {
			metricTree = metricTree + "|" + thisJO.getString("name");
		}

		if (apmcmDisplayMonitor.booleanValue()) {
			if (thisJO.optString("loc", null) != null) {
				metricTree = metricTree + "|" + (String)this.cpMap.get(thisJO.getString("loc"));
			}
		}

		Iterator thisJOKeys = thisJO.keys();
		while (thisJOKeys.hasNext()) {
			String thisKey = thisJOKeys.next().toString();

			if (thisJO.optJSONObject(thisKey) != null) {
				JSONObject innerJO = thisJO.getJSONObject(thisKey);
				metric_map.putAll(generateMetrics(innerJO.toString(), metricTree));
			}
			else if (thisJO.optJSONArray(thisKey) != null) {
				JSONArray innerJA = thisJO.optJSONArray(thisKey);
				for (int i = 0; i < innerJA.length(); i++)
				{
					if ((thisKey.equals("result")) || (thisKey.equals("monitors")) || (thisKey.equals("stats")))
						metric_map.putAll(generateMetrics(innerJA.getJSONObject(i).toString(), metricTree));
					else {
						metric_map.putAll(generateMetrics(innerJA.getJSONObject(i).toString(), metricTree + "|" + thisKey));
					}
				}
			}
			else
			{
				if ((thisKey.equals("code")) || (thisKey.equals("elapsed")) || 
						(thisKey.equals("info")) || (thisKey.equals("version")) || 
						(thisJO.optString(thisKey, "").length() == 0))
					continue;
				String thisValue = thisJO.getString(thisKey);

				if (thisKey.equals("descr")) {
					String rawErrorMetric = metricTree + ":" + (String)EPAConstants.apmcmMetrics.get("errors");
					metric_map.put(fixMetric(rawErrorMetric), "1");
				}

				if (thisKey.equals("color")) {
					String rawErrorMetric = metricTree + ":" + (String)EPAConstants.apmcmMetrics.get("colors");
					if (EPAConstants.apmcmColors.containsKey(thisValue))
						metric_map.put(fixMetric(rawErrorMetric), (String)EPAConstants.apmcmColors.get(thisValue));
					else {
						metric_map.put(fixMetric(rawErrorMetric), "0");
					}

				}

				if (EPAConstants.apmcmMetrics.containsKey(thisKey)) {
					thisKey = ((String)EPAConstants.apmcmMetrics.get(thisKey)).toString();
				}

				String rawMetric = metricTree + ":" + thisKey;
				metric_map.put(fixMetric(rawMetric), fixMetric(thisValue));
			}
		}

		return metric_map;
	}

	private HashMap<String, String> getCheckpoints(PropertiesUtils apmcmProperties)
			throws Exception
			{
		HashMap<String, String> returnCp = new HashMap<String, String>();

		String cpStr = "nkey=" + this.nkey + "&callback=" + "doCallback";
		String cpRequest = executeAPI("cp_list", cpStr);

		JSONArray cpJA = extractJSONArray(cpRequest, "checkpoints");

		for (int i = 0; i < cpJA.length(); i++) {
			JSONObject cpJO = cpJA.getJSONObject(i);
			if (cpJO.get("areas").toString().contains(","))
				returnCp.put(cpJO.get("loc").toString(), 
						cpJO.get("areas").toString().split(",")[1] + "|" + 
								cpJO.get("country_name") + "|" + cpJO.get("city"));
			else {
				returnCp.put(cpJO.get("loc").toString(), 
						cpJO.get("areas") + "|" + 
								cpJO.get("country_name") + "|" + cpJO.get("city"));
			}
		}

		return returnCp;
			}

	public HashMap<String, String> getCredits()
			throws Exception
			{
		HashMap<String, String> metric_map = new HashMap<String, String>();
		String creditsRequest = "";
		String creditsStr = "nkey=" + this.nkey + "&callback=" + "doCallback";
		creditsRequest = executeAPI("acct_credits", creditsStr);

		JSONArray creditJA = extractJSONArray(creditsRequest, "credits");

		for (int i = 0; i < creditJA.length(); i++) {
			JSONObject thisCreditJO = creditJA.getJSONObject(i);

			String thisKey = thisCreditJO.optString("type", "no type");
			String thisValue = thisCreditJO.optString("available", "0");

			if (EPAConstants.apmcmMetrics.containsKey(thisKey)) {
				thisKey = ((String)EPAConstants.apmcmMetrics.get(thisKey)).toString();
			}

			String rawMetric = "Credits:" + thisKey;
			metric_map.put(fixMetric(rawMetric), fixMetric(thisValue));
		}

		return metric_map;
			}

	public HashMap<String, String> getFolderMetrics(String folder) throws Exception
	{
		HashMap<String, String> metric_map = new HashMap<String, String>();

		String[] thisFolderRules = (String[])this.folderMap.get(folder);

		if (thisFolderRules.length == 1) {
			return metric_map;
		}

		if (folder.equals("root_folder")) {
			folder = "";
		}

		if (this.apmcmProperties.getProperty("apmcm.metrics.stats.folder", "false").equals("true")) {
			metric_map.putAll(getStats(folder, ""));
		}

		if ((thisFolderRules[0].equals("all_rules")) && (!folder.equals(""))) {
			if (this.apmcmProperties.getProperty("apmcm.metrics.public", "false").equals("true")) {
				metric_map.putAll(getPSP(folder, ""));
			}
			if (this.apmcmProperties.getProperty("apmcm.metrics.logs", "false").equals("true")) {
				metric_map.putAll(getLogs(folder, "", thisFolderRules.length - 1));
			}
			if (this.apmcmProperties.getProperty("apmcm.metrics.stats.rule", "false").equals("true"))
				for (int i = 0; i < thisFolderRules.length; i++) {
					if (thisFolderRules[i] == "all_rules") {
						continue;
					}
					metric_map.putAll(getStats(folder, thisFolderRules[i]));
				}
		}
		else
		{
			for (int j = 0; j < thisFolderRules.length; j++)
			{
				if (thisFolderRules[j].equals("all_rules")) {
					continue;
				}
				if (this.apmcmProperties.getProperty("apmcm.metrics.public", "false").equals("true")) {
					metric_map.putAll(getPSP(folder, thisFolderRules[j]));
				}
				if (this.apmcmProperties.getProperty("apmcm.metrics.stats.rule", "false").equals("true")) {
					metric_map.putAll(getStats(folder, thisFolderRules[j]));
				}
				if (this.apmcmProperties.getProperty("apmcm.metrics.logs", "false").equals("true")) {
					metric_map.putAll(getLogs(folder, thisFolderRules[j], 1));
				}
			}
		}

		return metric_map;
	}

	private String[] getFolders(String folderList)
			throws Exception
	{
		List<String> folderQueryOutput = new ArrayList<String>();
		String folderStr = "nkey=" + this.nkey + "&callback=" + "doCallback";
		String folderRequest = executeAPI("fldr_get", folderStr);

		JSONArray folderJA = extractJSONArray(folderRequest, "folders");

		folderQueryOutput.add("root_folder");
		for (int i = 0; i < folderJA.length(); i++) {
			JSONObject folderJO = folderJA.getJSONObject(i);

			if ((!folderJO.optString("active", "").equals("y")) && 
					(this.apmcmProperties.getProperty("apmcm.skip_inactive.folders", "").equals("true"))) continue;
			folderQueryOutput.add(folderJO.get("name").toString());
		}

		if (!folderList.equals("all_folders")) {
			return compareList(folderQueryOutput, folderList);
		}
		return (String[])folderQueryOutput.toArray(EPAConstants.kNoStringArrayProperties);
	}

	private HashMap<String, String[]> getFoldersAndRules(PropertiesUtils apmcmProperties)
			throws Exception
			{
		HashMap<String, String[]> foldersAndRules = new HashMap<String, String[]>();

		if ((apmcmProperties.getProperty("apmcm.folders", "").length() == 0) || 
				(apmcmProperties.getProperty("apmcm.folders", "").contains("all_folders")))
			this.apmcmFolders = getFolders("all_folders");
		else {
			this.apmcmFolders = getFolders(apmcmProperties.getProperty("apmcm.folders"));
		}

		for (int i = 0; i < this.apmcmFolders.length; i++)
		{
			String thisFolderProp = apmcmProperties.getProperty("apmcm.folder." + this.apmcmFolders[i], "");
			String[] rules;
			if ((thisFolderProp.length() == 0) || (thisFolderProp.equals("all_rules"))) {
				String[] allRules = getRules(this.apmcmFolders[i], "all_rules");
				rules = new String[allRules.length + 1];
				rules[0] = "all_rules";
				for (int j = 0; j < allRules.length; j++)
					rules[(j + 1)] = allRules[j];
			}
			else {
				rules = getRules(this.apmcmFolders[i], thisFolderProp);
			}
			if (rules.length > 0) {
				foldersAndRules.put(this.apmcmFolders[i], rules);
			}
		}
		return foldersAndRules;
			}

	private HashMap<String, String> getLogs(String folder, String rule, int numRules)
			throws Exception
			{
		String logRequest = "";
		String folderStr = "";
		String ruleStr = "";
		int numLogs = Integer.parseInt(this.apmcmProperties.getProperty("apmcm.numlogs")) * numRules;
		if ((folder.length() != 0) && (!folder.equals("root_folder")))
			folderStr = "&folder=" + folder;
		else {
			folder = "root_folder";
		}

		if (rule.length() != 0) {
			ruleStr = "&name=" + rule;
		}

		String logStr = "nkey=" + this.nkey + folderStr + ruleStr + 
				"&num=" + numLogs + "&reverse=y&callback=" + "doCallback";
		logRequest = executeAPI("rule_log", logStr);
		return generateMetrics(unpadJSON(logRequest), "Monitors|" + folder);
			}

	private HashMap<String, String> getPSP(String folder, String rule) throws Exception {
		String pspRequest = "";
		String folderStr = "";
		String ruleStr = "";

		if ((folder.length() != 0) && (!folder.equals("root_folder")))
			folderStr = "&folder=" + folder;
		else {
			folder = "root_folder";
		}

		if (rule.length() != 0) {
			ruleStr = "&name=" + rule;
		}

		String pspStr = "nkey=" + this.nkey + "&acct=" + this.apmcmUser + 
				folderStr + ruleStr + "&callback=" + "doCallback";
		pspRequest = executeAPI("rule_psp", pspStr);
		return generateMetrics(unpadJSON(pspRequest), "Monitors|" + folder);
	}

	private String[] getRules(String folder, String rulesList)
			throws Exception
	{
		List<String> ruleQueryOutput = new ArrayList<String>();
		String folderStr = "";
		if (!folder.equals("root_folder"))
			folderStr = "&folder=" + folder;
		else {
			folder = "";
		}

		String ruleStr = "nkey=" + this.nkey + folderStr + "&callback=" + "doCallback";
		String ruleRequest = executeAPI("rule_get", ruleStr);

		JSONArray ruleJA = extractJSONArray(ruleRequest, "rules");

		for (int i = 0; i < ruleJA.length(); i++) {
			JSONObject thisRuleJO = ruleJA.getJSONObject(i);
			if (!thisRuleJO.optString("folder", "").equals(folder)) {
				continue;
			}
			if ((!thisRuleJO.optString("active", "n").equals("y")) && 
					(this.apmcmProperties.getProperty("apmcm.skip_inactive.rules", "false").equals("true"))) continue;
			ruleQueryOutput.add(thisRuleJO.getString("name"));
		}

		if (!rulesList.equals("all_rules")) {
			return compareList(ruleQueryOutput, rulesList);
		}
		return (String[])ruleQueryOutput.toArray(EPAConstants.kNoStringArrayProperties);
	}

	private HashMap<String, String> getStats(String folder, String rule)
			throws Exception
			{
		String statsRequest = "";
		String folderStr = "";
		String ruleStr = "";

		if ((folder.length() != 0) && (!folder.equals("root_folder")))
			folderStr = "&folder=" + folder;
		else {
			folder = "root_folder";
		}

		if (rule.length() != 0) {
			ruleStr = "&name=" + rule;
			folder = folder + "|" + rule;
		}

		String statsStr = "nkey=" + this.nkey + "&acct=" + this.apmcmUser + folderStr + ruleStr + 
				"&start_date=" + this.todays_date + "&callback=" + "doCallback";
		statsRequest = executeAPI("rule_stats", statsStr);
		return generateMetrics(unpadJSON(statsRequest), "Monitors|" + folder);
			}

	private String getTodaysDate() throws Exception {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String todaysDate = null;
		todaysDate = dateFormat.format(calendar.getTime());
		return todaysDate;
	}

	private String login()
			throws Exception
	{
		String loginStr = "user=" + this.apmcmUser + "&password=" + this.apmcmPass + 
				"&callback=" + "doCallback";
		String loginRequest = executeAPI("acct_token", loginStr);
		JSONObject entireJO = new JSONObject(unpadJSON(loginRequest));

		if (entireJO.getInt("code") == 0) {
			JSONObject resultJO = entireJO.optJSONObject("result");
			if (resultJO != null) {
				return resultJO.optString("nkey", null);
			}
			return "Failed";
		}

		String errorStr = entireJO.optString("error", "No error given.");
		int errorCode = entireJO.optInt("code", -1);
		String errorInfo = entireJO.optString("info", "No information given.");

		System.err.println("Error: " + errorStr);
		System.err.println("Error Code: " + errorCode);
		System.err.println("Error Info: " + errorInfo);

		if (errorCode == 1000) {
			System.err.print("Login Failed.\nTry logging manually at " + 
					this.apmcmProperties.getProperty("apmcm.URL") + 
					"/" + "acct_token" + "\nwith the login credentials in your config file.\n" + 
					"NOTE: If this is your first time using the APM Cloud Monitor Agent,\n" + 
					"set your API password at : " + "http://www.watchmouse.com/en/change_passwd.php" + "\n" + 
					"Use the password provided to you by Watchmouse when you signed up.");
		}

		System.exit(1000);

		return "Failed";
	}

	public void printMetrics(HashMap<String, String> metric_map)
			throws Exception
	{
		Iterator<Map.Entry<String, String>> metricIt = metric_map.entrySet().iterator();
		while (metricIt.hasNext()) {
			Map.Entry<String, String> metricPairs = (Map.Entry<String, String>)metricIt.next();

			if (((String)metricPairs.getValue()).length() == 0)
				continue;
			String thisMetricType = returnMetricType((String)metricPairs.getValue());

			if (thisMetricType.equals("Float")) {
				metricPairs.setValue(((String)metricPairs.getValue()).split("\\.")[0]);
				thisMetricType = "IntCounter";
			}

			this.ps.println("<metric type=\"" + thisMetricType + "\" name=\"" + 
					"APM Cloud Monitor" + "|" + (String)metricPairs.getKey() + 
					"\" value=\"" + (String)metricPairs.getValue() + "\" />");
		}
	}

	public HashMap<String, String> resetMetrics(HashMap<String, String> metric_map)
			throws Exception
			{
		if (metric_map.size() != 0) {
			Iterator<Map.Entry<String, String>> metricIt = metric_map.entrySet().iterator();
			while (metricIt.hasNext()) {
				Map.Entry<String, String> metricPairs = (Map.Entry<String, String>)metricIt.next();

				if (!returnMetricType((String)metricPairs.getValue()).equals("StringEvent"))
					metric_map.put((String)metricPairs.getKey(), "0");
				else {
					metric_map.put((String)metricPairs.getKey(), "");
				}
			}
		}

		return metric_map;
			}

	public int retryConnection(int numRetriesLeft, String apmcmInfo)
	{
		System.err.println("Error connecting to Watchmouse for " + apmcmInfo);
		if (numRetriesLeft > 0) {
			System.err.println("Will retry connection " + numRetriesLeft + " more times.");
			numRetriesLeft--;
			try {
				Thread.sleep(60000L);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("Retried connection 10 times.");
		}
		return numRetriesLeft;
	}

	private String returnMetricType(String thisMetric)
	{
		String metricType = "StringEvent";
		try {
			new Integer(thisMetric);
			metricType = "IntCounter";
		} catch (NumberFormatException e) {
			try {
				new Long(thisMetric);
				metricType = "LongCounter";
			} catch (NumberFormatException ee) {
				try {
					new Float(thisMetric);
					metricType = "Float";
				} catch (NumberFormatException eee) {
					metricType = "StringEvent";
				}
			}
		}
		return metricType;
	}

	private String unpadJSON(String jsonWithPadding) throws Exception
	{
		String patternToMatch = "doCallback\\((.*)\\)([\n]*)";

		Pattern unpad = Pattern.compile(patternToMatch);
		Matcher matched = unpad.matcher(jsonWithPadding);

		if (matched.find()) {
			return matched.group(1);
		}
		return null;
	}
}

/* Location:           \\vmware-host\Shared Folders\Documents\1APM-Wily\EPA\CloudMonitor\APMCMAgent\lib\APMCMPlugin.jar
 * Qualified Name:     com.wily.fieldext.epaplugins.apmcloudmonitor.APMCMReader
 * JD-Core Version:    0.6.0
 */