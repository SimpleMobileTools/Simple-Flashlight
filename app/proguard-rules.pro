# The duolingo rtl-viewpager transitive dependency is excluded from the build
# (JitPack can no longer build it). The simplified app never instantiates the
# ViewPager-based commons views that reference it, so it is safe to tell R8 to
# ignore the missing class instead of failing the release build.
-dontwarn com.duolingo.open.rtlviewpager.**
