package ru.unisuite.contentservlet.service;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.process.Pipe;
import org.im4java.process.ProcessStarter;
import ru.unisuite.contentservlet.model.Content;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ResizeServiceIm4java implements ResizeService {

    public ResizeServiceIm4java() {
        ProcessStarter.setGlobalSearchPath("C:\\Program Files\\ImageMagick-7.0.10-Q16");
    }

    @Override
    public void writeResized(ContentRequest contentRequest, Content content, OutputStream out) throws IOException {
        InputStream in = content.getDataStream();

        shrinkStream(in, out, content.getExtension().toLowerCase(), contentRequest.getWidth(), contentRequest.getHeight()
                , contentRequest.getQuality());
    }

    private void shrinkStream(InputStream in, OutputStream out, String format, Integer width, Integer height, Byte quality) {
        IMOperation op = new IMOperation();
        op.addImage("-");
        op.resize(width, height, ">"); // ">" for shrink only
        if (quality != null) {
            op.quality((double) quality);
        }
        op.addImage(format + ":-");

        Pipe pipeIn = new Pipe(in, null);
        Pipe pipeOut = new Pipe(null, out);

        ConvertCmd convert = new ConvertCmd();
        convert.setInputProvider(pipeIn);
        convert.setOutputConsumer(pipeOut);

        try {
            convert.run(op);
        } catch (IOException | InterruptedException | IM4JavaException exception) {
            exception.printStackTrace();
        }
    }
}
