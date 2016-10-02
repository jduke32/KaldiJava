package pl.edu.pjwstk.kaldi.programs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pjwstk.kaldi.utils.LogStream;
import pl.edu.pjwstk.kaldi.utils.ProgramLauncher;
import pl.edu.pjwstk.kaldi.utils.Settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Shout {

    private final static Logger logger = LoggerFactory.getLogger(Shout.class);
    private final static LogStream logger_stdout = new LogStream(logger);
    private final static LogStream logger_stderr = new LogStream(logger, "ERR>> ");

    private static File shout_segment = new File(Settings.shout_dir,
            "shout_segment");

    private static File shout_cluster = new File(Settings.shout_dir,
            "shout_cluster");

    public static void test() throws FileNotFoundException {

        Field fields[] = Shout.class.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())
                    && field.getType().getName().equals("java.io.File")) {

                try {

                    File file = (File) field.get(null);

                    if (file == null || !file.exists())
                        throw new FileNotFoundException("" + file);

                } catch (IllegalArgumentException | IllegalAccessException e) {
                    logger.error("Internal error", e);
                }
            }
        }
    }

    public static void shout_segment(File audio_file, File seg_model,
                                     File out_mo) throws RuntimeException {

        String[] cmd = new String[]{shout_segment.getAbsolutePath(), "-a",
                audio_file.getAbsolutePath(), "-ams",
                seg_model.getAbsolutePath(), "-mo", out_mo.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

                logger.trace("shout_segment: " + audio_file.getName() + "->"
                        + out_mo.getName());
        launcher.run();
        logger.trace("Done.");

        if (launcher.getReturnValue() != 0)
            throw new RuntimeException("Retval: " + launcher.getReturnValue());
    }

    public static void shout_cluster(File audio_file, File seg_out,
                                     File out_mo, int max_clusters) throws RuntimeException {

        String[] cmd = null;

        if (max_clusters > 0) {
            cmd = new String[]{shout_cluster.getAbsolutePath(), "-a",
                    audio_file.getAbsolutePath(), "-mi",
                    out_mo.getAbsolutePath(), "-mo", seg_out.getAbsolutePath(),
                    "-l", audio_file.getName(), "-mc", "" + max_clusters};
        } else {
            cmd = new String[]{shout_cluster.getAbsolutePath(), "-a",
                    audio_file.getAbsolutePath(), "-mi",
                    seg_out.getAbsolutePath(), "-mo", out_mo.getAbsolutePath(),
                    "-l", audio_file.getName()};
        }

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("shout_cluster: " + audio_file.getName() + "->"
                + out_mo.getName());
        launcher.run();
        logger.trace("Done.");

        if (launcher.getReturnValue() != 0)
            throw new RuntimeException("Retval: " + launcher.getReturnValue());
    }

}
