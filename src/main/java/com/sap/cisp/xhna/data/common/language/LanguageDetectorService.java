package com.sap.cisp.xhna.data.common.language;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.optimaize.langdetect.DetectedLanguage;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;

/* Language Detector Service for G+ keyword search
 * The available languages refer to:
 * https://developers.google.com/+/web/api/rest/search#available-languages
 */

public class LanguageDetectorService {
    private List<LanguageProfile> languageProfiles = null;
    private static Logger logger = LoggerFactory
            .getLogger(LanguageDetectorService.class);

    // build language detector:
    private LanguageDetector languageDetector = null;

    public static LanguageDetectorService getInstance() {
        return LanguageDetectorServiceHolder.helper;
    }

    private static class LanguageDetectorServiceHolder {
        public static LanguageDetectorService helper = new LanguageDetectorService();
    }

    private LanguageDetectorService() {
        // private constructor
        try {
            languageProfiles = new LanguageProfileReader().readAllBuiltIn();
            languageDetector = LanguageDetectorBuilder
                    .create(NgramExtractors.standard())
                    .withProfiles(languageProfiles).shortTextAlgorithm(100)
                    .build();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.error("Cannot load the langusage profiles.", e);
        }
    }

    /**
     * Currently as a short term solution, only focus on "en" and "zh" first due
     * to the language-detector limitation: 1- short text/unclean text support
     * is not good; 2- multiple-language support is not good; 3- Address/People
     * name identification is not good
     * 
     * @param inputText
     * @return
     */
    public String getDetectedLanguage(String inputText) {
        /*
         * Some capturing group patterns for convenience.
         * 
         * CJK: Chinese, Japanese, Korean
         */
        Pattern CJK_PATTERN = Pattern
                .compile(
                        "([\\p{InHiragana}\\p{InKatakana}\\p{InBopomofo}\\p{InCJKCompatibilityIdeographs}\\p{InCJKUnifiedIdeographs}])",
                        Pattern.CASE_INSENSITIVE);
        // create a text object factory
        TextObjectFactory textObjectFactory = CommonTextObjectFactories
                .forDetectingShortCleanText();// CommonTextObjectFactories.forDetectingOnLargeText();
        // query:
        TextObject textObject = textObjectFactory.create().append(inputText);
        List<DetectedLanguage> result = languageDetector
                .getProbabilities(textObject);
        DetectedLanguage best = result.get(0);
        if (best != null
                && (best.getLocale().getLanguage().equalsIgnoreCase("en")
                        || best.getLocale().getLanguage()
                                .equalsIgnoreCase("zh")
                        || best.getLocale().getLanguage()
                                .equalsIgnoreCase("fr")
                        || best.getLocale().getLanguage()
                                .equalsIgnoreCase("de") || best.getLocale()
                        .getLanguage().equalsIgnoreCase("nl"))) {
            return best.getLocale().getLanguage();
        } else if (best != null
                && best.getLocale().getLanguage().equalsIgnoreCase("ko")
                || CJK_PATTERN.matcher(inputText).find()) {
            /*
             * for some cases which miss-detected as ko/ja, i.e. 天津 . This is a
             * difficult point!
             */

            textObject = textObjectFactory.create().append(inputText)
                    .append(" 中国");
            List<DetectedLanguage> resultTemp = languageDetector
                    .getProbabilities(textObject);
            DetectedLanguage bestTemp = resultTemp.get(0);
            if (bestTemp != null
                    && bestTemp.getLocale().getLanguage()
                            .equalsIgnoreCase("zh")) {
                return bestTemp.getLocale().getLanguage();
            } else {
                if (best != null) {
                    return best.getLocale().getLanguage();
                } else {
                    // language detector getProbabilities returns null, but CJK
                    // PATTERN matches
                    Pattern CJK_ZH = Pattern
                            .compile("[a-zA-Z0-9_\u4e00-\u9fa5]");
                    if (CJK_ZH.matcher(inputText).find())
                        return "zh";
                    else
                        return "en";
                }
            }
        } else {
            // Currently just return "en" as default language
            // i.e. Tianjin, Beijing, people name
            return "en";
        }
    }
}
