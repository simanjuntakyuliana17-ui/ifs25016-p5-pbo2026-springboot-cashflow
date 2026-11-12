package org.delcom.ifs25016springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadHandler;
import org.delcom.ifs25016springbootcashflow.adapter.storage.FileStorage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/** Menampung gambar yang sudah diunggah ke browser sampai tombol Simpan ditekan. */
final class PendingUpload {
    private String name;
    private byte[] data;

    boolean isPresent() {
        return data != null;
    }

    String name() {
        return name;
    }

    InputStream stream() {
        return new ByteArrayInputStream(data);
    }

    void clear() {
        name = null;
        data = null;
    }

    @SuppressWarnings("deprecation") // setAcceptedFileTypes masih berfungsi di Vaadin 25.3
    Upload component(String buttonText) {
        Upload upload = new Upload(UploadHandler.inMemory((meta, bytes) -> {
            this.name = meta.fileName();
            this.data = bytes;
        }));
        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/gif", "image/webp");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(FileStorage.MAX_BYTES);
        upload.setUploadButton(new Button(buttonText, VaadinIcon.UPLOAD.create()));
        upload.addFileRemovedListener(e -> clear());
        return upload;
    }
}
