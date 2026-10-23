/*
 *
 *   Copyright 2023 Einstein Blanco
 *
 *   Licensed under the GNU General Public License v3.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.gnu.org/licenses/gpl-3.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.android.geto.domain.framework

import com.android.geto.domain.model.SecureSetting
import com.android.geto.domain.model.SettingType

interface SecureSettingsWrapper {
    suspend fun canWriteSecureSettings(
        settingType: SettingType,
        key: String,
        value: String,
    ): Boolean

    suspend fun getSecureSettings(settingType: SettingType): List<SecureSetting>

    /**
     * Waits until Android has finished unbinding enabled accessibility services.
     *
     * Writing `Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES` only updates the underlying
     * setting synchronously. The AccessibilityManagerService that owns
     * the actual service bindings processes the change asynchronously, so apps that check
     * AccessibilityManager right after the write can still see the old services.
     *
     * @return true when no accessibility services remain enabled, false if [timeoutMillis] elapsed first.
     */
    suspend fun awaitAccessibilityServicesCleared(timeoutMillis: Long = 5_000L): Boolean
}
