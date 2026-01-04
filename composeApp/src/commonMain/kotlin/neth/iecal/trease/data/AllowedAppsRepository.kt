package neth.iecal.trease.data

import nethical.questphone.data.AppInfo


interface AllowedAppsRepository {
    suspend fun getAllowedPackages(): Set<String>
    suspend fun saveAllowedPackages(packages: Set<String>)
    fun getInstalledApps(): List<AppInfo>
    fun stopBlockerService() {}
}
