package dobby.io.request;

import common.logger.Logger;
import dobby.filter.Filter;
import dobby.filter.FilterOrder;
import dobby.filter.FilterType;
import dobby.io.HttpContext;

import java.io.File;
import java.util.Map;

public class RemoveTemporaryFilesPostFilter implements Filter {
    private static final Logger LOGGER = new Logger(RemoveTemporaryFilesPostFilter.class);

    @Override
    public String getName() {
        return "RemoveTemporaryFilesPostFilter";
    }

    @Override
    public FilterType getType() {
        return FilterType.POST;
    }

    @Override
    public int getOrder() {
        return FilterOrder.REMOVE_TEMPORARY_FILES_POST_FILTER.getOrder();
    }

    @Override
    public boolean run(HttpContext ctx) {
        final Request request = ctx.getRequest();
        final Map<String, File> temporaryFiles = request.getFiles();

        if (temporaryFiles == null || temporaryFiles.isEmpty()) {
            return true;
        }

        for (Map.Entry<String, File> entry : temporaryFiles.entrySet()) {
            File file = entry.getValue();
            if (file != null && file.exists()) {
                if (!file.delete()) {
                    LOGGER.error("Failed to delete temporary file: " + file.getAbsolutePath());
                } else {
                    LOGGER.debug("Deleted temporary file: " + file.getAbsolutePath());
                }
            }
        }

        return true;
    }
}
