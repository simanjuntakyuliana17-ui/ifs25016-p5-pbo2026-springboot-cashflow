package org.delcom.ifs25016springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;

/**
 * Memberi id pada elemen &lt;input&gt; di dalam field Vaadin (bukan pada pembungkus vaadin-text-field),
 * agar alat uji otomatis dapat menemukan dan mengisi input lewat selector seperti #login-username.
 */
final class InputIds {
    private InputIds() {
    }

    static void onInput(Component field, String id) {
        field.getElement().executeJs("""
                let tries = 0;
                const apply = () => {
                  const input = this.querySelector('input');
                  if (!input) {
                    if (tries++ < 100) { requestAnimationFrame(apply); }
                    return;
                  }
                  input.id = $0;
                  const label = this.querySelector('label');
                  if (label) { label.setAttribute('for', $0); }
                };
                apply();
                """, id);
    }
}
