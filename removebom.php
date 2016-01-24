<?php

function rglob($pattern, $flags = 0) {
    $files = glob($pattern, $flags); 
    foreach (glob(dirname($pattern).'/*', GLOB_ONLYDIR|GLOB_NOSORT) as $dir) {
        $files = array_merge($files, rglob($dir.'/'.basename($pattern), $flags));
    }
    return $files;
}

$BOM = "\xEF\xBB\xBF";
foreach (rglob("**/*.java") as $file) {
	$content = file_get_contents($file);
	if (substr($content, 0, 3) == "$BOM") {
		echo "$file\n";
		$content = substr($content, 3);
		file_put_contents($file, $content);
	}
}
