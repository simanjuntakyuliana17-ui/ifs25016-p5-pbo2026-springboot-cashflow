package org.delcom.ifs25016springbootcashflow.adapter.storage;

import org.delcom.ifs25016springbootcashflow.domain.exception.ValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** Menyimpan file unggahan (foto profil dan bukti transaksi) ke folder UPLOAD_DIR. */
public class FileStorage {
    public static final int MAX_BYTES = 2 * 1024 * 1024;
    private static final Set<String> ALLOWED = Set.of("png", "jpg", "jpeg", "gif", "webp");
    private final Path root;

    public FileStorage(String uploadDir) {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException ex) {
            throw new IllegalStateException("Folder upload tidak dapat dibuat: " + root, ex);
        }
    }

    /** Menyimpan gambar dengan nama acak (UUID + ekstensi); mengembalikan nama file relatif. */
    public String save(String originalName, InputStream in) throws ValidationException {
        String ext = extensionOf(originalName);
        if (!ALLOWED.contains(ext)) {
            throw new ValidationException("Format gambar harus png, jpg, jpeg, gif, atau webp");
        }
        try {
            byte[] bytes = in.readNBytes(MAX_BYTES + 1);
            if (bytes.length > MAX_BYTES) {
                throw new ValidationException("Ukuran gambar maksimal 2 MB");
            }
            String name = UUID.randomUUID() + "." + ext;
            Files.write(root.resolve(name), bytes);
            return name;
        } catch (IOException ex) {
            throw new ValidationException("Gagal menyimpan file");
        }
    }

    /** Menghapus file bila ada. Hanya nama file yang dipakai (mencegah path traversal). */
    public void delete(String path) {
        if (path == null || path.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(root.resolve(Path.of(path).getFileName().toString()));
        } catch (IOException ignored) {
            // file yatim tidak mengganggu aplikasi
        }
    }

    public Path root() {
        return root;
    }

    /** Lokasi resource untuk Spring MVC (berakhiran slash). */
    public String resourceLocation() {
        String uri = root.toUri().toString();
        return uri.endsWith("/") ? uri : uri + "/";
    }

    private static String extensionOf(String name) {
        if (name == null || !name.contains(".")) {
            return "";
        }
        return name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
