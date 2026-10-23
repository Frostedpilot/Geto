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
package com.android.geto.domain.usecase

import com.android.geto.domain.common.dispatcher.Dispatcher
import com.android.geto.domain.common.dispatcher.GetoDispatchers
import com.android.geto.domain.framework.SecureSettingsWrapper
import com.android.geto.domain.model.AppSettingsResult
import com.android.geto.domain.model.SettingType
import com.android.geto.domain.repository.AppSettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ApplyAppSettingsUseCase @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    private val secureSettingsWrapper: SecureSettingsWrapper,
    @param:Dispatcher(GetoDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(componentName: String): AppSettingsResult = withContext(defaultDispatcher) {
        val appSettings =
            appSettingsRepository.getAppSettingsByComponentName(componentName = componentName)

        if (appSettings.isEmpty()) return@withContext AppSettingsResult.EmptyAppSettings

        val enabledAppSettings = appSettings.filter { it.enabled }

        if (enabledAppSettings.isEmpty()) return@withContext AppSettingsResult.DisabledAppSettings

        try {
            if (enabledAppSettings.all {
                    secureSettingsWrapper.canWriteSecureSettings(
                        settingType = it.settingType,
                        key = it.key,
                        value = it.valueOnLaunch,
                    )
                }
            ) {
                if (enabledAppSettings.any {
                        it.settingType == SettingType.SECURE && it.key in ACCESSIBILITY_SETTING_KEYS
                    }
                ) {
                    secureSettingsWrapper.awaitAccessibilityServicesCleared()
                }

                AppSettingsResult.Success
            } else {
                AppSettingsResult.Failure
            }
        } catch (_: SecurityException) {
            AppSettingsResult.NoPermission
        } catch (_: IllegalArgumentException) {
            AppSettingsResult.InvalidValues
        }
    }

    private companion object {
        val ACCESSIBILITY_SETTING_KEYS = setOf(
            "accessibility_enabled",
            "enabled_accessibility_services",
        )
    }
}
