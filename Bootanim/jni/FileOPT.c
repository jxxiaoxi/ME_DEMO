#include <string.h>
#include <stdio.h>
#include <jni.h>

#include <unistd.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <sys/types.h>

#include <utils/Log.h>
#include <utils/Log.h>
#include <stdlib.h>


#define BUFFER_SIZE 1024

//#define  ALOGE(...)  __android_log_print(ANDROID_LOG_ERROR,"subtitleBitmap",__VA_ARGS__) tag

jstring Java_com_example_bootanim_FileOPT_OpenFile(JNIEnv *env, jobject thiz) {
            ALOGI("jnioooo");
	return (*env)->NewStringUTF(env, " open xxxxxxxcccxxxx");
}



jstring Java_com_example_bootanim_FileOPT_WriteFile(JNIEnv *env, jobject thiz, jstring oldpath, jstring newpath) {//oldFile = "/system/build.prop";
        ALOGI("jniooooxxx");
        int from_fd;
        int to_fd;
        long file_len=0;
    	int ret=1;
    	char buffer[BUFFER_SIZE];
    	char *ptr;

        const char *oldfilea = (*env)->GetStringUTFChars(env, oldpath, 0);
        const char *newfilea = (*env)->GetStringUTFChars(env,newpath,0); 
            ALOGI("jniooooffff:%s",oldfilea);

       ALOGI("jniooooxxx from_fd opentt"); 
        if((from_fd=open(newfilea, O_RDONLY | O_NOFOLLOW))==-1) { 
    	        ALOGI("jniooooxxx from_fd fail");
    	        return "0"; 
    	    } 
       ALOGI("jniooooxxx to_fd open");
         if((to_fd=open(oldfilea, O_WRONLY|O_NOFOLLOW))==-1) { 
    	       ALOGI("jniooooxxx to_fd= fail"); 
    	        return "0"; 
    	    }

            file_len= lseek(from_fd,0L,SEEK_END);
    	    lseek(from_fd,0L,SEEK_SET);
            ALOGI("jniooooxxx while"); 
            while(ret) { 
    	        ret= read(from_fd, buffer, BUFFER_SIZE);
    	        if(ret==-1){
    	            ALOGI("jniooooxxx ret==-1"); 
    	            return "0";		
    	        }
    	        write(to_fd, buffer, ret);
    	        file_len-=ret;
    	        bzero(buffer,BUFFER_SIZE);

    	    } 
            ALOGI("jniooooxxx close"); 
            close(from_fd); 
    	    close(to_fd); 
    	    //exit(0);


    return (*env)->NewStringUTF(env, " opecccccccc!");
}


