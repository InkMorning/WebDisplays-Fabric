package net.montoyo.wd.client;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.montoyo.wd.utilities.Log;
import org.cef.callback.CefCallback;
import org.cef.handler.CefResourceHandler;
import org.cef.misc.IntRef;
import org.cef.misc.StringRef;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

/**
 * Replaces MCEF's own mod:// handler: it asks the ClassLoader for a resource name that starts with
 * a slash, which never resolves under Fabric's Knot, so every mod:// page came back missing.
 */
public class WDModScheme implements CefResourceHandler {

    private byte[] data;
    private int offset;
    private String mime;

    @Override
    public boolean processRequest(CefRequest request, CefCallback callback) {
        data = null;
        offset = 0;
        mime = null;

        String url = request.getURL();
        String prefix = url.startsWith("webdisplays://") ? "webdisplays://" : (url.startsWith("mod://") ? "mod://" : null);
        if (prefix == null)
            return false;

        String rest = url.substring(prefix.length());
        int slash = rest.indexOf('/');
        if (slash < 0)
            return false;

        String domain = rest.substring(0, slash).toLowerCase(Locale.ROOT);
        String file = rest.substring(slash + 1).toLowerCase(Locale.ROOT);

        int query = file.indexOf('?');
        if (query >= 0)
            file = file.substring(0, query);

        if (domain.isEmpty() || file.isEmpty() || file.contains(".."))
            return false;

        Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(domain);
        if (container.isEmpty())
            return false;

        // MCEF's own convention keeps mod pages under html/, the plain assets/ path is a fallback.
        String[] candidates = {"assets/" + domain + "/html/" + file, "assets/" + domain + "/" + file};
        for (String candidate : candidates) {
            Optional<Path> found = container.get().findPath(candidate);
            if (found.isEmpty())
                continue;

            try (InputStream in = Files.newInputStream(found.get())) {
                data = readAll(in);
            } catch (IOException ex) {
                Log.warningEx("Could not read mod resource %s", ex, candidate);
                return false;
            }

            int dot = file.lastIndexOf('.');
            String type = dot >= 0 ? WDScheme.mapMime(file.substring(dot + 1)) : null;
            mime = type != null ? type : "application/octet-stream";
            Log.info("WD mod:// serving %s (%d bytes)", candidate, data.length);
            callback.Continue();
            return true;
        }

        Log.warning("WD resource handler: nothing matched %s in %s/%s", prefix, domain, file);
        return false;
    }

    @Override
    public void getResponseHeaders(CefResponse response, IntRef responseLength, StringRef stringRef) {
        if (data == null) {
            response.setStatus(404);
            response.setStatusText("Not Found");
            responseLength.set(0);
            return;
        }

        response.setMimeType(mime);
        response.setStatus(200);
        response.setStatusText("OK");
        responseLength.set(data.length);
    }

    @Override
    public boolean readResponse(byte[] buf, int bytesToRead, IntRef bytesRead, CefCallback callback) {
        if (data == null || offset >= data.length) {
            data = null;
            return false;
        }

        int count = Math.min(bytesToRead, data.length - offset);
        System.arraycopy(data, offset, buf, 0, count);
        offset += count;
        bytesRead.set(count);
        return true;
    }

    @Override
    public void cancel() {
        data = null;
    }

    private static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(Math.max(in.available(), 4096));
        byte[] chunk = new byte[8192];
        int read;
        while ((read = in.read(chunk)) > 0)
            out.write(chunk, 0, read);
        return out.toByteArray();
    }
}
