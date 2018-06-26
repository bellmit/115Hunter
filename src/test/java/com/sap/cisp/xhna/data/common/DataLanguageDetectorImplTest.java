package com.sap.cisp.xhna.data.common;

import com.google.common.base.Optional;
import com.optimaize.langdetect.DetectedLanguage;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import com.sap.cisp.xhna.data.common.language.LanguageDetectorService;

import java.io.IOException;
import java.util.List;





import java.util.regex.Pattern;

import junit.framework.TestCase;

/**
 * @author Fabian Kessler
 */
public class DataLanguageDetectorImplTest extends TestCase {


    public void testShortTextAlgo() throws IOException {
        LanguageDetector detector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                .shortTextAlgorithm(50).prefixFactor(1.5).suffixFactor(2.0)
                .withProfiles(new LanguageProfileReader().readAllBuiltIn())
                .build();
        runTests(detector);
    }


    public void testLongTextAlgo() throws IOException {
        LanguageDetector detector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                .shortTextAlgorithm(0)
                .withProfiles(new LanguageProfileReader().readAllBuiltIn())
                .build();
        runTests(detector);
    }
    
    public void testMytext() throws IOException {
      //load all languages:
        List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAllBuiltIn();

        //build language detector:
        LanguageDetector languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                .withProfiles(languageProfiles).shortTextAlgorithm(100)
                .build();

        //create a text object factory
        TextObjectFactory textObjectFactory = //CommonTextObjectFactories.forDetectingOnLargeText();
        CommonTextObjectFactories.forDetectingShortCleanText();
        //query:
        TextObject textObject = textObjectFactory.create().append("天津").append(" 中国");
        Optional<LdLocale> lang = languageDetector.detect(textObject);
        assertEquals(lang.orNull().getLanguage(),"zh");
        List<DetectedLanguage> result = languageDetector.getProbabilities(textObject);
        DetectedLanguage best = result.get(0);
        assertEquals(best.getLocale().getLanguage(), "zh");
        assertTrue(best.getProbability() >= 0.9999d);
    }
    
    public void testLanguageDetectorService() throws IOException {
        LanguageDetectorService service = LanguageDetectorService.getInstance();
        assertEquals(service.getDetectedLanguage("This is english text"), "en");
        assertEquals(service.getDetectedLanguage("天津"), "zh");
        assertEquals(service.getDetectedLanguage("天津爆炸"), "zh");
        assertEquals(service.getDetectedLanguage("天津"), "zh");
        assertEquals(service.getDetectedLanguage("李克强"), "zh");
        assertEquals(service.getDetectedLanguage("一带一路"), "zh");
        assertEquals(service.getDetectedLanguage("Barack Obama"), "en");
        assertEquals(service.getDetectedLanguage("Tianjin"), "en");
        assertEquals(service.getDetectedLanguage("China rates cut"), "en");
        assertEquals(service.getDetectedLanguage("China"), "en");
        assertEquals(service.getDetectedLanguage("Xi jinping"), "en");
        assertEquals(service.getDetectedLanguage("Vladimir Putin"), "en");
        assertEquals(service.getDetectedLanguage("One road"), "en");
        assertEquals(service.getDetectedLanguage("Ceci est un texte français."), "fr");
        assertEquals(service.getDetectedLanguage("Dit is een Nederlandse tekst."), "nl");
        assertEquals(service.getDetectedLanguage("Dies ist eine deutsche Text."), "de");
        assertEquals(service.getDetectedLanguage("世锦赛 2015"), "zh");
        assertEquals(service.getDetectedLanguage("世界選手権"), "zh");
        assertEquals(service.getDetectedLanguage("反法西斯70周年"), "zh");
        assertEquals(service.getDetectedLanguage("ありがとうございます"), "ja");
        assertEquals(service.getDetectedLanguage("감사합니다"), "ko");
        
        /*
         * Some capturing group patterns for convenience.
         *
         * CJK: Chinese, Japanese, Korean
         */
        Pattern CJK_ANS = Pattern.compile(
                "([\\p{InHiragana}\\p{InKatakana}\\p{InBopomofo}\\p{InCJKCompatibilityIdeographs}\\p{InCJKUnifiedIdeographs}])",
                Pattern.CASE_INSENSITIVE);
        assertEquals(CJK_ANS.matcher("世锦赛2015 beijing").find(), true);
        assertEquals(CJK_ANS.matcher("习近平 主席").find(), true);
        
        Pattern CJK_ZH = Pattern
                .compile("[a-zA-Z0-9_\u4e00-\u9fa5]");
        assertEquals(CJK_ZH.matcher("习近平 主席").find(), true);
        assertEquals(CJK_ZH.matcher("世锦赛 2015").find(), true);
        assertEquals(CJK_ZH.matcher("反法西斯70周年").find(), true);
    }

    /**
     * Note: this works stable with getProbabilities(), the best language comes first.
     * However, with detect(), it would not always work for Dutch.
     * The short text algorithm returns a stable 0.99something, but not high enough for the current standards.
     * The long text algorithm returns either a 0.9999something (often), or a much lower 0.7something (sometimes).
     */
    private void runTests(LanguageDetector detector) {
        assertEquals(detector.getProbabilities(text("Chinese one road one belt")).get(0).getLocale().getLanguage(), "en");
        assertEquals(detector.getProbabilities(text("Ceci est un texte français.")).get(0).getLocale().getLanguage(), "fr");
        assertEquals(detector.getProbabilities(text("Dit is een Nederlandse tekst.")).get(0).getLocale().getLanguage(), "nl");
        assertEquals(detector.getProbabilities(text("Dies ist eine deutsche Text")).get(0).getLocale().getLanguage(), "de");
        assertEquals(detector.getProbabilities(text("សព្វវចនាធិប្បាយសេរីសម្រាប់អ្នកទាំងអស់គ្នា។" +"នៅក្នុងវិគីភីឌាភាសាខ្មែរឥឡូវនេះមាន ១១៩៨រូបភាព សមាជិក១៥៣៣៣នាក់ និងមាន៤៥៨៣អត្ថបទ។")).get(0).getLocale().getLanguage(), "km");
        assertEquals(detector.getProbabilities(text("ありがとうございます")).get(0).getLocale().getLanguage(), "ja");
    }

    private CharSequence text(CharSequence text) {
        return CommonTextObjectFactories.forDetectingShortCleanText().forText( text );
    }

}