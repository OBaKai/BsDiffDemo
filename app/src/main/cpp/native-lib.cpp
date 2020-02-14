#include <jni.h>
#include <string>

extern "C" {
    extern int bs_main(int argc, const char *argv[]);
}

extern "C" JNIEXPORT void JNICALL
Java_com_llk_bsd_MainActivity_bsPatch(
        JNIEnv *env, jobject,
        jstring old_, jstring patch_, jstring output_){
    const char *old = env->GetStringUTFChars(old_, 0);
    const char *patch = env->GetStringUTFChars(patch_, 0);
    const char *output = env->GetStringUTFChars(output_, 0);

    const char *argv[] = {"", old, output, patch};
    bs_main(4, argv);

    env->ReleaseStringUTFChars(old_, old);
    env->ReleaseStringUTFChars(patch_, patch);
    env->ReleaseStringUTFChars(output_, output);
}