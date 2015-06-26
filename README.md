# SlidingDrawer for Android
Draggable sliding drawer provides an easy way to slide from any side of your screen.

## Download
Simply add the following dependency to your *`build.gradle`* file to use the latest version:

```groovy
dependencies {
    repositories {
        mavenCentral()
    }
    compile 'com.github.ali-rezaei.android.client.customview:sliding-drawer:0.1.2'
}
```

## Integration
Due to simplicity and lightness, this container is currently based on a FrameLayout. Place it in a **_RelativeLayout_** of your XML layout file.

## XML

```xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:custom="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
  <com.github.ali.android.client.customview.view.SlidingDrawer
    android:id="@+id/slidingDrawer"
    android:layout_width="match_parent"
    android:layout_height="800px"
    android:layout_alignParentTop | alignParentBottom | alignParentRight | alignParentLeft = "true" 
    android:background="@color/drawer_background"
    custom:offsetDistance="200px"
    custom:stickTo="top|bottom|right|left" >
    
    <RelativeLayout
      android:layout_width="match_parent"
      android:layout_height="match_parent">

      <fragment
        android:id="@+id/controlFragment"
        android:name="demo.controllers.ControlFragment"
        android:layout_width="@dimen/button_size"
        android:layout_height="@dimen/button_size"
        android:layout_centerInParent="true" />

    </RelativeLayout>
  </com.github.ali.android.client.customview.view.SlidingDrawer>
```

### Properties
- *`offsetDistance`* - a reference to the dimension of the desired size for the layer to offset in the screen in order for it to be directly swipable to open.
- *`stickTo`* - an enum that determines to where the container should stick to. ‘left’ sticks the container to the left side of the screen. ‘right’ sticks the container to the right side of the screen, and so on with ‘top‘ and ‘bottom‘ states. Default is ‘bottom’.

### Contributing
Contributions are very welcome. If you found a bug in the library or wanted a feature and thought you can fix it yourself, fork + pull request and i will appreciate it!


