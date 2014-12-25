package org.quanqi.gradle.signing

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException

/**
 * Created by cindy on 12/24/14.
 */
class AndroidApplicationSigning implements Plugin<Project> {

    public static final String KEY_ANDROID_KEY_STORE_CONFIG = "ANDROID_KEY_STORE_CONFIG"


    @Override
    void apply(Project project) {

        if (!project.plugins.findPlugin("com.android.application") && !project.plugins.findPlugin("android")) {
            throw new ProjectConfigurationException("The android plugin must be applied to the project", null)
        }

        def keysXmlFile = null
        if (project.hasProperty(KEY_ANDROID_KEY_STORE_CONFIG)) {
            keysXmlFile = new File(KEY_ANDROID_KEY_STORE_CONFIG)
        } else {
            keysXmlFile = new File(System.getProperty('user.home'), '.android_key_store.xml')
        }

        if (!keysXmlFile.exists()) {
            throw new FileNotFoundException(keysXmlFile.getAbsolutePath() + " dose not exits.")
        }

        def slurper = new XmlSlurper();
        def keyStores = slurper.parse(keysXmlFile)

        for (def keyStore : keyStores.children()) {
            def keyStoreName = keyStore.getProperty('store-name').text()
            def keyStorePath = keyStore.getProperty('store-path').text()
            def keyStorePassword = keyStore.getProperty('store-password').text()

            for (def alias : keyStore.getProperty('aliases').children()) {
                def aliasName = alias.getProperty('alias-name').text()
                def aliasPassword = alias.getProperty('alias-password').text()

                def signingConfig = keyStoreName + '_' + aliasName
                project.android.signingConfigs {
                    "$signingConfig" {
                        storeFile = new File(keyStorePath)
                        storePassword = keyStorePassword
                        keyAlias = aliasName
                        keyPassword = aliasPassword
                    }
                }
            }
        }
    }
}
