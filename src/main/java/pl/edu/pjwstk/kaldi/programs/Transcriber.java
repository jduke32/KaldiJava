package pl.edu.pjwstk.kaldi.programs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pjwstk.kaldi.utils.FileUtils;
import pl.edu.pjwstk.kaldi.utils.LogStream;
import pl.edu.pjwstk.kaldi.utils.ProgramLauncher;
import pl.edu.pjwstk.kaldi.utils.Settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class Transcriber {

    private final static Logger logger = LoggerFactory.getLogger(Transcriber.class);
    private final static LogStream logger_stdout = new LogStream(logger);
    private final static LogStream logger_stderr = new LogStream(logger, "ERR>> ");

    private static File transcriber_bin;
    private static File rules;
    private static File replacement;

    public static void init() {
        transcriber_bin = new File(Settings.transcriber_dir, "transcriber");
        rules = new File(Settings.transcriber_dir, "transcription.rules");
        replacement = new File(Settings.transcriber_dir, "replacement.rules");
    }

    public static void test() throws FileNotFoundException {
        if (!Settings.transcriber_dir.exists())
            throw new FileNotFoundException(
                    Settings.transcriber_dir.getAbsolutePath());
        if (!transcriber_bin.exists())
            throw new FileNotFoundException(transcriber_bin.getAbsolutePath());
        if (!rules.exists())
            throw new FileNotFoundException(rules.getAbsolutePath());
        if (!replacement.exists())
            throw new FileNotFoundException(replacement.getAbsolutePath());
    }

    public static void transcribe(File vocab, String vocab_enc, File dict,
                                  String dict_enc, boolean add_sent_boundaries) throws IOException {

        File temp_vocab = File.createTempFile("voc", ".txt");
        File temp_dic = File.createTempFile("dic", ".txt");

        ArrayList<String> sent = new ArrayList<>();
        sent.add("<s>");
        sent.add("</s>");
        sent.add("<unk>");
        sent.add("<UNK>");
        sent.add("sil");
        sent.add("SIL");
        sent.add("-pau-");

        FileUtils.removeLines(vocab, vocab_enc, temp_vocab, vocab_enc, sent,
                true);

        String[] cmd = new String[]{transcriber_bin.getAbsolutePath(), "-r",
                rules.getAbsolutePath(), "-w", replacement.getAbsolutePath(),
                "-i", temp_vocab.getAbsolutePath(), "-ie", vocab_enc, "-o",
                temp_dic.getAbsolutePath(), "-oe", dict_enc};

        ProgramLauncher launcher = new ProgramLauncher(cmd);

        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("Transcribing...");
        launcher.run();
        logger.trace("Done.");

        ArrayList<String> dic = new ArrayList<>();
        dic.add("SIL sil");
        dic.add("<UNK> sil");

        if (add_sent_boundaries) {
            dic.add("<s> sil");
            dic.add("</s> sil");
        }

        FileUtils.appendLines(temp_dic, dict_enc, dict, dict_enc, dic, true);

        temp_vocab.delete();
        temp_dic.delete();
    }

}
