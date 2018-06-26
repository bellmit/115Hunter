package com.sap.cisp.xhna.data.executor.traditional;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP GET SOURCE URL UTIL
 */
public class GetSourceUrlUtil {
    private static Logger logger = LoggerFactory
            .getLogger(GetSourceUrlUtil.class);
    private static final int LOOP_THRESHOLD = 10;
    private static final Pattern patternHTTPWWW = Pattern.compile(
            "(?:^((?!http://www).)*$)", Pattern.CASE_INSENSITIVE
                    | Pattern.DOTALL);
    private static final Pattern patternHTTPSWWW = Pattern.compile(
            "(?:^((?!https://www).)*$)", Pattern.CASE_INSENSITIVE
                    | Pattern.DOTALL);
    // end with htm/html is fine
    // i.e http://usa.chinadaily.com.cn/china/2016-01/20/content_23165891.htm
    private static final Pattern patternHTML = Pattern.compile(
            "((?:((htm)|(html)))$)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    // end with jpg?
    private static final Pattern patternJPG = Pattern.compile(
            "((?:((jpg)|(jpeg)))$)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * Overload method for traditional rss url parse
     * 
     */
    public static String sendGet(String url, boolean isRedirectMandatory,
            int counter) {
        return sendGet(url, isRedirectMandatory, false, counter);
    }

    public static String sendGet(String url, boolean isRedirectMandatory,
            boolean isSocialUrl, int counter) {
        String result = null;
        HttpURLConnection conn = null;
        try {
            if (counter > LOOP_THRESHOLD) {
                //For https, it always return response code 200.
                //If the url does not match the current patterns, we cannot just return null...
                logger.debug("Cannot get the further url after {} loops, just return the url.", LOOP_THRESHOLD);
                return url;
            }
            //https cannot get location header.
            if(url.startsWith("https")) {
                return url;
            }

            if (!url.startsWith("http")) {
                url = "http://" + url;
            }

            URL realUrl = new URL(url);
            conn = (HttpURLConnection) realUrl.openConnection();
            conn.setRequestMethod("GET");

            // simulate a browser
            conn.setReadTimeout(50000);
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.addRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.118 Safari/537.36");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            // prevent redirect
            if (!isRedirectMandatory)
                conn.setInstanceFollowRedirects(false);
            else
                conn.setInstanceFollowRedirects(true);

            conn.connect();
            int code = conn.getResponseCode();
            logger.debug("Response code --> {}, url --> {}, counter --> {}",
                    code, url, counter);
            // If the first response is 301, and the second response is 303,
            // should return the url, otherwise it will be
            // redirected to login page.(For NYTimes case)
            if (code == HttpURLConnection.HTTP_OK
                    || code == HttpURLConnection.HTTP_SEE_OTHER) {
                if (!patternHTTPWWW.matcher(url).find()
                        || !patternHTTPSWWW.matcher(url).find()
                        || patternHTML.matcher(url).find()
                        || patternJPG.matcher(url).find()) {
                    logger.debug("*** Match the pattern to return !!! ****");
                    if (!isSocialUrl) {
                        // remove params start with ?
                        // e.g.:http://www.cnn.com/politics/cuba-terror-list/index.html?rss=xxx&rss2=xxx
                        Pattern pattern = Pattern.compile("(^.*)\\?.*$");
                        Matcher matcher = pattern.matcher(url);
                        if (matcher.find()) {
                            return matcher.group(1);
                        }

                        // remove params start with #
                        // e.g.:http://www.cnn.com/politics/cuba-terror-list/index.html#rss=xxx&rss2=xxx
                        Pattern pattern2 = Pattern.compile("(^.*)#.*$");
                        Matcher matcher2 = pattern2.matcher(url);
                        if (matcher2.find()) {
                            return matcher2.group(1);
                        }
                    }
                    // return url directly if no param included
                    return url;
                } else {
                    return sendGet(url, false, isSocialUrl, ++counter);
                }
            }

            // error case:
            // http://rss.cnn.com/~r/rss/edition_world/~3/650gMxuGyko/orig-natpkg-uk-plane-sideways-landing-wind.storyful.html
            // http://www.cnn.com/video/data/2.0/video/tv/2015/01/20/orig-natpkg-uk-plane-sideways-landing-wind.storyful.html?utm_source=feedburner&utm_medium=feed&utm_campaign=Feed%3A+rss%2Fedition_world+%28RSS%3A+CNNi+-+World%29
            // redirect url is stored in location field or Location field
            logger.debug("Response: ");
            Map<String, List<String>> responseHeaders = conn.getHeaderFields();
            for (Map.Entry<String, List<String>> entry : responseHeaders
                    .entrySet()) {
                String key = entry.getKey();
                List<String> values = entry.getValue();
                for (String value : values) {
                    if (key != null) {
                        logger.debug(key + ": " + value);
                    } else {
                        logger.debug(value);
                    }
                }
            }

            List<String> locationInLowerCase = conn.getHeaderFields().get(
                    "location");
            List<String> locationInUpperCase = conn.getHeaderFields().get(
                    "Location");
            if ((locationInLowerCase == null || locationInLowerCase.isEmpty())
                    && locationInUpperCase != null) {
                result = locationInUpperCase.get(0);
            } else if (locationInLowerCase != null) {
                result = locationInLowerCase.get(0);
            }

            // 404 error
            if (result == null) {
                logger.error("Cannot get the Location header.");
                return null;
            }
            // some location field only store the tail of url, we should
            // concatenate the website manually
            if (!result.startsWith("http")) {
                // http://www.cnn.com/politics/cuba-terror-list/index.html?rss=xxx&rss2=xxx
                // matcher.group(1) will be http://www.cnn.com/
                Pattern pattern = Pattern
                        .compile("^((http://[0-9a-zA-Z\\.]+/)?).*$");
                logger.debug("Manual compose result for url {}", url);
                Matcher matcher = pattern.matcher(url);
                if (matcher.find()) {
                    logger.debug("Matcher group(1) -->  {}", matcher.group(1));
                    result = matcher.group(1) + result;
                }
            }

            if (code == HttpURLConnection.HTTP_MOVED_TEMP
                    || code == HttpURLConnection.HTTP_MOVED_PERM) {
                // || code == HttpURLConnection.HTTP_SEE_OTHER) {
                isRedirectMandatory = true;
            }
            // recursion if result is not the source url
            logger.debug(
                    "Continue with result url {} ; redirect -> {} ; counter -> {}",
                    result, isRedirectMandatory, counter);
            return sendGet(result, isRedirectMandatory, isSocialUrl, counter++);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in getting source url : {}", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return result;
    }
}
