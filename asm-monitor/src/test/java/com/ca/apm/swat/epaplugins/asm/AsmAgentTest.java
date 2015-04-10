package com.ca.apm.swat.epaplugins.asm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import com.ca.apm.swat.epaplugins.asm.monitor.MonitorFactory;


/**
 * Basic functional ASM test. Read from supplied JSON string.
 *   Run with -DDEBUG=true for debug output.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class AsmAgentTest extends FileTest {

    @Override
    public void setup() {
        super.setup();
        
        // we need to load the the monitoring station map
        try {
            requestHelper.getMonitoringStations();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("error getting monitoring stations: " + e.getMessage());
        }
    }

    /**
     * Basic functional ASM test. Read from supplied JSON string.
     */
    @Test
    public void test() {
        String json = "{\"version\":\"8.2.9a\",\"code\":0,\"tz\":\"Europe/Vienna\",\"gmtoffset\":\"1\",\"elapsed\":\"2301.0840\",\"result\":[{\"name\":\"CA Wiki RBM\",\"rid\":\"386491\",\"start\":\"2014-12-10 00:24:54\",\"end\":\"2014-12-10 00:29:54\",\"repeat\":\"1\",\"result\":\"1042\",\"type\":\"0\",\"descr\":\"Execution timed out after 30\",\"ttime\":\"22833\",\"rtime\":\"17\",\"ctime\":\"783\",\"ptime\":\"20879\",\"dtime\":\"22033\",\"dsize\":\"325623\",\"loc\":\"gv\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20737497\"},{\"name\":\"CA Wiki\",\"rid\":\"381101\",\"start\":\"2014-12-10 02:57:14\",\"end\":\"2014-12-10 03:02:14\",\"repeat\":\"1\",\"result\":\"1044\",\"type\":\"0\",\"descr\":\"Timeout while connecting\",\"ttime\":null,\"rtime\":null,\"ctime\":null,\"ptime\":null,\"dtime\":null,\"dsize\":null,\"loc\":\"vi\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20737617\"},{\"name\":\"CA Wiki\",\"rid\":\"381101\",\"start\":\"2014-12-10 13:01:42\",\"end\":\"2014-12-10 13:06:42\",\"repeat\":\"1\",\"result\":\"1044\",\"type\":\"0\",\"descr\":\"Timeout while connecting\",\"ttime\":null,\"rtime\":null,\"ctime\":null,\"ptime\":null,\"dtime\":null,\"dsize\":null,\"loc\":\"mi\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738089\"},{\"name\":\"CA Wiki\",\"rid\":\"381101\",\"start\":\"2014-12-10 13:01:53\",\"end\":\"2014-12-10 13:06:53\",\"repeat\":\"1\",\"result\":\"1044\",\"type\":\"2\",\"descr\":\"Timeout while connecting\",\"ttime\":null,\"rtime\":null,\"ctime\":null,\"ptime\":null,\"dtime\":null,\"dsize\":null,\"loc\":\"se\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738091\"},{\"name\":\"CA Wiki RBM\",\"rid\":\"386491\",\"start\":\"2014-12-10 14:50:21\",\"end\":\"2014-12-10 14:55:21\",\"repeat\":\"1\",\"result\":\"1042\",\"type\":\"0\",\"descr\":\"Execution timed out after 30\",\"ttime\":\"19513\",\"rtime\":\"3\",\"ctime\":\"1445\",\"ptime\":\"16596\",\"dtime\":\"18065\",\"dsize\":\"325629\",\"loc\":\"ks\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738175\"},{\"name\":\"CA Wiki\",\"rid\":\"381101\",\"start\":\"2014-12-10 17:31:43\",\"end\":\"2014-12-10 17:36:43\",\"repeat\":\"1\",\"result\":\"1044\",\"type\":\"0\",\"descr\":\"Timeout while connecting\",\"ttime\":null,\"rtime\":null,\"ctime\":null,\"ptime\":null,\"dtime\":null,\"dsize\":null,\"loc\":\"ki\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738301\"},{\"name\":\"CA Wiki\",\"rid\":\"381101\",\"start\":\"2014-12-10 17:31:53\",\"end\":\"2014-12-10 17:36:53\",\"repeat\":\"1\",\"result\":\"1044\",\"type\":\"2\",\"descr\":\"Timeout while connecting\",\"ttime\":null,\"rtime\":null,\"ctime\":null,\"ptime\":null,\"dtime\":null,\"dsize\":null,\"loc\":\"du\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738303\"},{\"name\":\"CA Wiki\",\"rid\":\"381101\",\"start\":\"2014-12-10 18:07:54\",\"end\":\"2014-12-10 18:12:54\",\"repeat\":\"1\",\"result\":\"1044\",\"type\":\"0\",\"descr\":\"Timeout while connecting\",\"ttime\":\"2041\",\"rtime\":\"155\",\"ctime\":\"138\",\"ptime\":\"1748\",\"dtime\":\"1748\",\"dsize\":\"506\",\"loc\":\"b5\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738331\"},{\"name\":\"CA Wiki\",\"rid\":\"381101\",\"start\":\"2014-12-10 18:08:04\",\"end\":\"2014-12-10 18:13:04\",\"repeat\":\"1\",\"result\":\"1044\",\"type\":\"2\",\"descr\":\"Timeout while connecting\",\"ttime\":null,\"rtime\":null,\"ctime\":null,\"ptime\":null,\"dtime\":null,\"dsize\":null,\"loc\":\"gr\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738333\"},{\"name\":\"CA Wiki\",\"rid\":\"381101\",\"start\":\"2014-12-10 18:13:09\",\"end\":\"2014-12-10 18:18:09\",\"repeat\":\"1\",\"result\":\"1044\",\"type\":\"0\",\"descr\":\"Timeout while connecting\",\"ttime\":\"753\",\"rtime\":\"79\",\"ctime\":\"158\",\"ptime\":\"516\",\"dtime\":\"516\",\"dsize\":\"506\",\"loc\":\"ki\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738337\"},{\"name\":\"CA Wiki\",\"rid\":\"381101\",\"start\":\"2014-12-10 18:13:20\",\"end\":\"2014-12-10 18:18:20\",\"repeat\":\"1\",\"result\":\"1044\",\"type\":\"2\",\"descr\":\"Timeout while connecting\",\"ttime\":null,\"rtime\":null,\"ctime\":null,\"ptime\":null,\"dtime\":null,\"dsize\":null,\"loc\":\"hu\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738339\"},{\"name\":\"CA Wiki\",\"rid\":\"381101\",\"start\":\"2014-12-10 18:18:24\",\"end\":\"2014-12-10 18:23:24\",\"repeat\":\"1\",\"result\":\"1044\",\"type\":\"0\",\"descr\":\"Timeout while connecting\",\"ttime\":null,\"rtime\":null,\"ctime\":null,\"ptime\":null,\"dtime\":null,\"dsize\":null,\"loc\":\"nl\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738343\"},{\"name\":\"CA Wiki\",\"rid\":\"381101\",\"start\":\"2014-12-10 18:18:35\",\"end\":\"2014-12-10 18:23:35\",\"repeat\":\"1\",\"result\":\"1044\",\"type\":\"2\",\"descr\":\"Timeout while connecting\",\"ttime\":null,\"rtime\":null,\"ctime\":null,\"ptime\":null,\"dtime\":null,\"dsize\":null,\"loc\":\"c4\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738345\"},{\"name\":\"CA Wiki RBM\",\"rid\":\"386491\",\"start\":\"2014-12-10 18:21:52\",\"end\":\"2014-12-10 18:26:52\",\"repeat\":\"1\",\"result\":\"502\",\"type\":\"0\",\"descr\":\"Proxy Error\",\"ttime\":\"14689\",\"rtime\":\"6\",\"ctime\":\"250\",\"ptime\":\"14430\",\"dtime\":\"14433\",\"dsize\":\"91\",\"loc\":\"lx\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738347\"},{\"name\":\"CA Wiki RBM\",\"rid\":\"386491\",\"start\":\"2014-12-10 18:32:31\",\"end\":\"2014-12-10 18:37:31\",\"repeat\":\"1\",\"result\":\"1042\",\"type\":\"0\",\"descr\":\"Execution timed out after 30\",\"ttime\":\"27934\",\"rtime\":\"1\",\"ctime\":\"269\",\"ptime\":\"27375\",\"dtime\":\"27664\",\"dsize\":\"325629\",\"loc\":\"cl\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738357\"},{\"name\":\"CA Wiki\",\"rid\":\"381101\",\"start\":\"2014-12-10 18:43:46\",\"end\":\"2014-12-10 18:48:46\",\"repeat\":\"1\",\"result\":\"1044\",\"type\":\"0\",\"descr\":\"Timeout while connecting\",\"ttime\":null,\"rtime\":null,\"ctime\":null,\"ptime\":null,\"dtime\":null,\"dsize\":null,\"loc\":\"ny\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738367\"},{\"name\":\"CA Wiki RBM\",\"rid\":\"386491\",\"start\":\"2014-12-10 18:43:47\",\"end\":\"2014-12-10 18:48:47\",\"repeat\":\"1\",\"result\":\"502\",\"type\":\"0\",\"descr\":\"Proxy Error\",\"ttime\":\"10510\",\"rtime\":\"1\",\"ctime\":\"174\",\"ptime\":\"10332\",\"dtime\":\"10335\",\"dsize\":\"91\",\"loc\":\"l3\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738369\"},{\"name\":\"CA Wiki RBM\",\"rid\":\"386491\",\"start\":\"2014-12-10 18:54:50\",\"end\":\"2014-12-10 18:59:50\",\"repeat\":\"1\",\"result\":\"1042\",\"type\":\"0\",\"descr\":\"Execution timed out after 30\",\"ttime\":\"28952\",\"rtime\":\"7\",\"ctime\":\"802\",\"ptime\":\"27100\",\"dtime\":\"28143\",\"dsize\":\"325629\",\"loc\":\"i4\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738381\"},{\"name\":\"CA Wiki\",\"rid\":\"381101\",\"start\":\"2014-12-10 18:58:53\",\"end\":\"2014-12-10 19:03:53\",\"repeat\":\"1\",\"result\":\"1044\",\"type\":\"0\",\"descr\":\"Timeout while connecting\",\"ttime\":null,\"rtime\":null,\"ctime\":null,\"ptime\":null,\"dtime\":null,\"dsize\":null,\"loc\":\"ny\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738383\"},{\"name\":\"CA Wiki RBM\",\"rid\":\"386491\",\"start\":\"2014-12-10 19:01:52\",\"end\":\"2014-12-10 19:06:52\",\"repeat\":\"1\",\"result\":\"502\",\"type\":\"0\",\"descr\":\"Proxy Error\",\"ttime\":\"6641\",\"rtime\":\"6\",\"ctime\":\"162\",\"ptime\":\"6470\",\"dtime\":\"6473\",\"dsize\":\"91\",\"loc\":\"at\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738387\"},{\"name\":\"CA Wiki RBM\",\"rid\":\"386491\",\"start\":\"2014-12-10 19:07:16\",\"end\":\"2014-12-10 19:12:16\",\"repeat\":\"1\",\"result\":\"1042\",\"type\":\"0\",\"descr\":\"Execution timed out after 30\",\"ttime\":\"29592\",\"rtime\":\"2\",\"ctime\":\"1129\",\"ptime\":\"27070\",\"dtime\":\"28461\",\"dsize\":\"325629\",\"loc\":\"ar\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738393\"},{\"name\":\"CA Wiki RBM\",\"rid\":\"386491\",\"start\":\"2014-12-10 19:13:08\",\"end\":\"2014-12-10 19:18:08\",\"repeat\":\"1\",\"result\":\"502\",\"type\":\"0\",\"descr\":\"Proxy Error\",\"ttime\":\"15677\",\"rtime\":\"9\",\"ctime\":\"811\",\"ptime\":\"14852\",\"dtime\":\"14857\",\"dsize\":\"91\",\"loc\":\"ar\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738399\"},{\"name\":\"CA Wiki\",\"rid\":\"381101\",\"start\":\"2014-12-10 19:19:12\",\"end\":\"2014-12-10 19:24:12\",\"repeat\":\"1\",\"result\":\"1044\",\"type\":\"0\",\"descr\":\"Timeout while connecting\",\"ttime\":null,\"rtime\":null,\"ctime\":null,\"ptime\":null,\"dtime\":null,\"dsize\":null,\"loc\":\"r1\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738405\"},{\"name\":\"CA Wiki\",\"rid\":\"381101\",\"start\":\"2014-12-10 19:19:22\",\"end\":\"2014-12-10 19:24:22\",\"repeat\":\"1\",\"result\":\"1044\",\"type\":\"2\",\"descr\":\"Timeout while connecting\",\"ttime\":null,\"rtime\":null,\"ctime\":null,\"ptime\":null,\"dtime\":null,\"dsize\":null,\"loc\":\"i4\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738407\"},{\"name\":\"CA Wiki\",\"rid\":\"381101\",\"start\":\"2014-12-10 19:24:28\",\"end\":\"2014-12-10 19:29:28\",\"repeat\":\"1\",\"result\":\"1044\",\"type\":\"0\",\"descr\":\"Timeout while connecting\",\"ttime\":null,\"rtime\":null,\"ctime\":null,\"ptime\":null,\"dtime\":null,\"dsize\":null,\"loc\":\"sf\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738411\"},{\"name\":\"CA Wiki\",\"rid\":\"381101\",\"start\":\"2014-12-10 19:24:38\",\"end\":\"2014-12-10 19:29:38\",\"repeat\":\"1\",\"result\":\"1044\",\"type\":\"2\",\"descr\":\"Timeout while connecting\",\"ttime\":null,\"rtime\":null,\"ctime\":null,\"ptime\":null,\"dtime\":null,\"dsize\":null,\"loc\":\"b3\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738413\"},{\"name\":\"CA Wiki RBM\",\"rid\":\"386491\",\"start\":\"2014-12-10 19:25:06\",\"end\":\"2014-12-10 19:30:06\",\"repeat\":\"1\",\"result\":\"502\",\"type\":\"0\",\"descr\":\"Proxy Error\",\"ttime\":\"16988\",\"rtime\":\"2\",\"ctime\":\"441\",\"ptime\":\"16537\",\"dtime\":\"16545\",\"dsize\":\"91\",\"loc\":\"nl\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738415\"},{\"name\":\"CA Wiki\",\"rid\":\"381101\",\"start\":\"2014-12-10 19:29:45\",\"end\":\"2014-12-10 19:34:45\",\"repeat\":\"1\",\"result\":\"1044\",\"type\":\"0\",\"descr\":\"Timeout while connecting\",\"ttime\":null,\"rtime\":null,\"ctime\":null,\"ptime\":null,\"dtime\":null,\"dsize\":null,\"loc\":\"vi\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738419\"},{\"name\":\"CA Wiki\",\"rid\":\"381101\",\"start\":\"2014-12-10 19:29:56\",\"end\":\"2014-12-10 19:34:56\",\"repeat\":\"1\",\"result\":\"1044\",\"type\":\"2\",\"descr\":\"Timeout while connecting\",\"ttime\":null,\"rtime\":null,\"ctime\":null,\"ptime\":null,\"dtime\":null,\"dsize\":null,\"loc\":\"b2\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738421\"},{\"name\":\"CA Wiki RBM\",\"rid\":\"386491\",\"start\":\"2014-12-10 19:30:43\",\"end\":\"2014-12-10 19:35:43\",\"repeat\":\"1\",\"result\":\"1042\",\"type\":\"0\",\"descr\":\"Execution timed out after 30\",\"ttime\":\"0\",\"rtime\":\"0\",\"ctime\":\"0\",\"ptime\":\"0\",\"dtime\":\"0\",\"dsize\":\"0\",\"loc\":\"hk\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738423\"},{\"name\":\"CA Wiki RBM\",\"rid\":\"386491\",\"start\":\"2014-12-10 19:31:22\",\"end\":\"2014-12-10 19:36:22\",\"repeat\":\"1\",\"result\":\"1042\",\"type\":\"2\",\"descr\":\"Execution timed out after 30\",\"ttime\":\"0\",\"rtime\":\"0\",\"ctime\":\"0\",\"ptime\":\"0\",\"dtime\":\"0\",\"dsize\":\"0\",\"loc\":\"ar\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738425\"},{\"name\":\"CA Wiki\",\"rid\":\"381101\",\"start\":\"2014-12-10 19:35:03\",\"end\":\"2014-12-10 19:40:03\",\"repeat\":\"1\",\"result\":\"1044\",\"type\":\"0\",\"descr\":\"Timeout while connecting\",\"ttime\":null,\"rtime\":null,\"ctime\":null,\"ptime\":null,\"dtime\":null,\"dsize\":null,\"loc\":\"os\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738427\"},{\"name\":\"CA Wiki\",\"rid\":\"381101\",\"start\":\"2014-12-10 19:35:13\",\"end\":\"2014-12-10 19:40:13\",\"repeat\":\"1\",\"result\":\"1044\",\"type\":\"2\",\"descr\":\"Timeout while connecting\",\"ttime\":null,\"rtime\":null,\"ctime\":null,\"ptime\":null,\"dtime\":null,\"dsize\":null,\"loc\":\"sf\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738429\"},{\"name\":\"CA Wiki RBM\",\"rid\":\"386491\",\"start\":\"2014-12-10 19:36:53\",\"end\":\"2014-12-10 19:41:53\",\"repeat\":\"1\",\"result\":\"1042\",\"type\":\"0\",\"descr\":\"Execution timed out after 30\",\"ttime\":\"0\",\"rtime\":\"0\",\"ctime\":\"0\",\"ptime\":\"0\",\"dtime\":\"0\",\"dsize\":\"0\",\"loc\":\"lx\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738431\"},{\"name\":\"CA Wiki RBM\",\"rid\":\"386491\",\"start\":\"2014-12-10 19:37:27\",\"end\":\"2014-12-10 19:42:27\",\"repeat\":\"1\",\"result\":\"1042\",\"type\":\"2\",\"descr\":\"Execution timed out after 30\",\"ttime\":\"0\",\"rtime\":\"0\",\"ctime\":\"0\",\"ptime\":\"0\",\"dtime\":\"0\",\"dsize\":\"0\",\"loc\":\"ms\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738433\"},{\"name\":\"CA Wiki\",\"rid\":\"381101\",\"start\":\"2014-12-10 19:40:19\",\"end\":\"2014-12-10 19:45:19\",\"repeat\":\"1\",\"result\":\"1044\",\"type\":\"0\",\"descr\":\"Timeout while connecting\",\"ttime\":null,\"rtime\":null,\"ctime\":null,\"ptime\":null,\"dtime\":null,\"dsize\":null,\"loc\":\"it\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738435\"},{\"name\":\"CA Wiki\",\"rid\":\"381101\",\"start\":\"2014-12-10 19:40:29\",\"end\":\"2014-12-10 19:45:29\",\"repeat\":\"1\",\"result\":\"1044\",\"type\":\"2\",\"descr\":\"Timeout while connecting\",\"ttime\":null,\"rtime\":null,\"ctime\":null,\"ptime\":null,\"dtime\":null,\"dsize\":null,\"loc\":\"ch\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738437\"},{\"name\":\"CA Wiki RBM\",\"rid\":\"386491\",\"start\":\"2014-12-10 19:42:58\",\"end\":\"2014-12-10 19:47:58\",\"repeat\":\"1\",\"result\":\"1042\",\"type\":\"0\",\"descr\":\"Execution timed out after 30\",\"ttime\":\"0\",\"rtime\":\"0\",\"ctime\":\"0\",\"ptime\":\"0\",\"dtime\":\"0\",\"dsize\":\"0\",\"loc\":\"sf\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738439\"},{\"name\":\"CA Wiki RBM\",\"rid\":\"386491\",\"start\":\"2014-12-10 19:43:31\",\"end\":\"2014-12-10 19:48:31\",\"repeat\":\"1\",\"result\":\"1042\",\"type\":\"2\",\"descr\":\"Execution timed out after 30\",\"ttime\":\"0\",\"rtime\":\"0\",\"ctime\":\"0\",\"ptime\":\"0\",\"dtime\":\"0\",\"dsize\":\"0\",\"loc\":\"ri\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738441\"},{\"name\":\"CA Wiki\",\"rid\":\"381101\",\"start\":\"2014-12-10 19:45:37\",\"end\":\"2014-12-10 19:50:37\",\"repeat\":\"1\",\"result\":\"1044\",\"type\":\"0\",\"descr\":\"Timeout while connecting\",\"ttime\":null,\"rtime\":null,\"ctime\":null,\"ptime\":null,\"dtime\":null,\"dsize\":null,\"loc\":\"ph\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738443\"},{\"name\":\"CA Wiki\",\"rid\":\"381101\",\"start\":\"2014-12-10 19:45:47\",\"end\":\"2014-12-10 19:50:47\",\"repeat\":\"1\",\"result\":\"1044\",\"type\":\"2\",\"descr\":\"Timeout while connecting\",\"ttime\":null,\"rtime\":null,\"ctime\":null,\"ptime\":null,\"dtime\":null,\"dsize\":null,\"loc\":\"a2\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738445\"},{\"name\":\"CA Wiki RBM\",\"rid\":\"386491\",\"start\":\"2014-12-10 19:49:09\",\"end\":\"2014-12-10 19:54:09\",\"repeat\":\"1\",\"result\":\"1042\",\"type\":\"0\",\"descr\":\"Execution timed out after 30\",\"ttime\":\"0\",\"rtime\":\"0\",\"ctime\":\"0\",\"ptime\":\"0\",\"dtime\":\"0\",\"dsize\":\"0\",\"loc\":\"au\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738447\"},{\"name\":\"CA Wiki RBM\",\"rid\":\"386491\",\"start\":\"2014-12-10 19:49:45\",\"end\":\"2014-12-10 19:54:45\",\"repeat\":\"1\",\"result\":\"1042\",\"type\":\"2\",\"descr\":\"Execution timed out after 30\",\"ttime\":\"0\",\"rtime\":\"0\",\"ctime\":\"0\",\"ptime\":\"0\",\"dtime\":\"0\",\"dsize\":\"0\",\"loc\":\"a3\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738449\"},{\"name\":\"CA Wiki\",\"rid\":\"381101\",\"start\":\"2014-12-10 19:50:53\",\"end\":\"2014-12-10 19:55:53\",\"repeat\":\"1\",\"result\":\"1044\",\"type\":\"0\",\"descr\":\"Timeout while connecting\",\"ttime\":null,\"rtime\":null,\"ctime\":null,\"ptime\":null,\"dtime\":null,\"dsize\":null,\"loc\":\"st\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738451\"},{\"name\":\"CA Wiki\",\"rid\":\"381101\",\"start\":\"2014-12-10 19:51:03\",\"end\":\"2014-12-10 19:56:03\",\"repeat\":\"1\",\"result\":\"503\",\"type\":\"2\",\"descr\":\"Service Temporarily Unavailable\",\"ttime\":\"492\",\"rtime\":\"2\",\"ctime\":\"117\",\"ptime\":\"373\",\"dtime\":\"373\",\"dsize\":\"307\",\"loc\":\"se\",\"alerts\":\"0\",\"ipaddr\":\"199.119.123.234\",\"id\":\"20738453\"},{\"name\":\"CA Wiki RBM\",\"rid\":\"386491\",\"start\":\"2014-12-10 19:55:15\",\"end\":\"2014-12-10 20:00:15\",\"repeat\":\"1\",\"result\":\"1042\",\"type\":\"0\",\"descr\":\"Execution timed out after 30\",\"ttime\":\"0\",\"rtime\":\"0\",\"ctime\":\"0\",\"ptime\":\"0\",\"dtime\":\"0\",\"dsize\":\"0\",\"loc\":\"b5\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738455\"},{\"name\":\"CA Wiki\",\"rid\":\"381101\",\"start\":\"2014-12-10 19:55:59\",\"end\":\"2014-12-10 20:00:59\",\"repeat\":\"1\",\"result\":\"1044\",\"type\":\"0\",\"descr\":\"Timeout while connecting\",\"ttime\":null,\"rtime\":null,\"ctime\":null,\"ptime\":null,\"dtime\":null,\"dsize\":null,\"loc\":\"cy\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738457\"},{\"name\":\"CA Wiki\",\"rid\":\"381101\",\"start\":\"2014-12-10 19:56:09\",\"end\":\"2014-12-10 20:01:09\",\"repeat\":\"1\",\"result\":\"1044\",\"type\":\"2\",\"descr\":\"Timeout while connecting\",\"ttime\":null,\"rtime\":null,\"ctime\":null,\"ptime\":null,\"dtime\":null,\"dsize\":null,\"loc\":\"st\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738459\"},{\"name\":\"CA Wiki RBM\",\"rid\":\"386491\",\"start\":\"2014-12-10 19:55:54\",\"end\":\"2014-12-10 20:00:54\",\"repeat\":\"1\",\"result\":\"1042\",\"type\":\"2\",\"descr\":\"Execution timed out after 30\",\"ttime\":\"0\",\"rtime\":\"0\",\"ctime\":\"0\",\"ptime\":\"0\",\"dtime\":\"0\",\"dsize\":\"0\",\"loc\":\"at\",\"alerts\":\"0\",\"ipaddr\":null,\"id\":\"20738461\"}]}";
        String propertyFileName = "target/test-classes/AppSyntheticMonitor.properties"; 
        
        try {
            Properties properties = AsmReader.readPropertiesFromFile(propertyFileName);
            properties.setProperty(DISPLAY_STATIONS, FALSE);

            HashMap<String, String> map = MonitorFactory.getAllMonitorsMonitor()
                    .generateMetrics(json, "Monitors|CA");

            SortedSet<String> set = new TreeSet<String>();
            for (Iterator<String> it = map.keySet().iterator(); it.hasNext();) {
                String key = it.next();
                set.add(key.concat(" = ").concat(map.get(key)));
            }

            if (DEBUG) {
                for (Iterator<String> it = set.iterator(); it.hasNext();) {
                    String value = it.next();
                    System.out.println(value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("error during test: " + e.getMessage());
        }
    }
}