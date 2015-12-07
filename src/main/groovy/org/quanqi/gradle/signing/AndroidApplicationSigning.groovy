package org.quanqi.gradle.signing

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException

/**
 * Created by cindy on 12/24/14.
 */
class AndroidApplicationSigning implements Plugin<Project> {

    public static final String KEY_ANDROID_KEY_STORE_CONFIG = "ANDROID_KEY_STORE_CONFIG"

    static def adjustPath(path) {
        if (path.startsWith('~/')) {
            path = path.replaceFirst('~', System.getProperty('user.home'))
            return path
        } else {
            def file = new File(path)
            if(file.absolute) {
                return path
            } else {
                return new File(System.getProperty('user.home'), '.android_keys/' + path).absolutePath
            }
        }
    }

    @Override
    void apply(Project project) {
        project.extensions.create("key_store", AndroidApplicationSigningExtension.class)

        if (project.hasProperty("${project.key_store.store}")) {

        }

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
            println("signing configration not")
        }

        def slurper = new XmlSlurper();
        def keyStores = slurper.parse(keysXmlFile)

        for (def keyStore : keyStores.children()) {
            def keyStoreName = keyStore.getProperty('store-name').text()
            def keyStorePath = keyStore.getProperty('store-path').text()
            keyStorePath = adjustPath(keyStorePath)
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
