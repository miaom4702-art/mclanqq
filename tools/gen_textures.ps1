Add-Type -AssemblyName System.Drawing

$base = Join-Path (Split-Path -Parent $PSScriptRoot) "src\main\resources\assets\mclanqq\textures\gui"
New-Item -ItemType Directory -Path $base -Force | Out-Null

function New-Bmp([int]$w, [int]$h) {
    $bmp = New-Object System.Drawing.Bitmap($w, $h, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.Clear([System.Drawing.Color]::FromArgb(0, 0, 0, 0))
    $g.Dispose()
    return $bmp
}

function Px($bmp, [int]$x, [int]$y, [int]$r, [int]$g, [int]$b, [int]$a = 255) {
    if ($x -lt 0 -or $y -lt 0 -or $x -ge $bmp.Width -or $y -ge $bmp.Height) { return }
    $bmp.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($a, $r, $g, $b))
}

function Rect($bmp, [int]$x, [int]$y, [int]$w, [int]$h, [int]$r, [int]$g, [int]$b, [int]$a = 255) {
    for ($i = 0; $i -lt $w; $i++) { for ($j = 0; $j -lt $h; $j++) { Px $bmp ($x + $i) ($y + $j) $r $g $b $a } }
}

# ---------- panel.png (24x24, 9-slice border 6, tintable grayscale) ----------
$p = New-Bmp 24 24
for ($y = 0; $y -lt 24; $y++) {
    for ($x = 0; $x -lt 24; $x++) {
        # rounded corners radius 3
        $cx = if ($x -lt 3) { 3 } elseif ($x -gt 20) { 20 } else { $x }
        $cy = if ($y -lt 3) { 3 } elseif ($y -gt 20) { 20 } else { $y }
        $dx = $x - $cx; $dy = $y - $cy
        if (($dx * $dx + $dy * $dy) -gt 9) { continue }
        $edge = [Math]::Min([Math]::Min($x, 23 - $x), [Math]::Min($y, 23 - $y))
        if ($edge -le 1) { Px $p $x $y 150 150 150 }       # border (tint -> darker)
        elseif ($x -le 3 -or $y -le 3) { Px $p $x $y 255 255 255 } # highlight
        else { Px $p $x $y 235 235 235 }                    # fill
    }
}
$p.Save("$base\panel.png", [System.Drawing.Imaging.ImageFormat]::Png); $p.Dispose()

# ---------- bg.png (64x64 sakura night tile) ----------
$bg = New-Bmp 64 64
for ($y = 0; $y -lt 64; $y++) {
    for ($x = 0; $x -lt 64; $x++) {
        if ((([int]($x / 8) + [int]($y / 8)) % 2) -eq 0) { Px $bg $x $y 0x2a 0x20 0x46 }
        else { Px $bg $x $y 0x24 0x1b 0x38 }
    }
}
$stars = @(
    @(6,7,'ff9ed8'), @(21,4,'ffffff'), @(40,9,'8be9fd'), @(55,18,'ffd6ec'),
    @(12,26,'ffffff'), @(33,30,'ff9ed8'), @(50,38,'ffd6ec'), @(8,45,'8be9fd'),
    @(27,52,'ffffff'), @(44,58,'ff9ed8'), @(58,48,'ffffff'), @(17,59,'ffd6ec')
)
foreach ($s in $stars) {
    $x = $s[0]; $y = $s[1]; $c = $s[2]
    $r = [Convert]::ToInt32($c.Substring(0,2),16); $gg = [Convert]::ToInt32($c.Substring(2,2),16); $bb = [Convert]::ToInt32($c.Substring(4,2),16)
    Px $bg $x $y $r $gg $bb
    Px $bg ($x - 1) $y ([Math]::Max(0,$r-40)) $gg $bb
    Px $bg ($x + 1) $y ([Math]::Max(0,$r-40)) $gg $bb
    Px $bg $x ($y - 1) $r ([Math]::Max(0,$gg-40)) $bb
    Px $bg $x ($y + 1) $r ([Math]::Max(0,$gg-40)) $bb
}
$bg.Save("$base\bg.png", [System.Drawing.Imaging.ImageFormat]::Png); $bg.Dispose()

# ---------- mascot.png (16x16 chibi cat) ----------
$m = New-Bmp 16 16
# ears
Rect $m 3 1 2 3 0xd4 0x6a 0xa0
Rect $m 11 1 2 3 0xd4 0x6a 0xa0
Rect $m 3 2 1 2 0xff 0xb3 0xd9
Rect $m 12 2 1 2 0xff 0xb3 0xd9
# head
for ($y = 3; $y -le 13; $y++) { for ($x = 2; $x -le 13; $x++) {
    $cx = if ($x -lt 4) { 4 } elseif ($x -gt 11) { 11 } else { $x }
    $cy = if ($y -lt 5) { 5 } elseif ($y -gt 12) { 12 } else { $y }
    $dx = $x - $cx; $dy = $y - $cy
    if (($dx*$dx + $dy*$dy) -gt 12) { continue }
    Px $m $x $y 0xff 0xd9 0xec
} }
# outline-ish darker bottom
for ($x = 4; $x -le 11; $x++) { Px $m $x 13 0xd4 0x6a 0xa0 }
# eyes (big)
foreach ($ex in @(5,10)) {
    Rect $m $ex 7 2 3 0x3a 0x2b 0x57
    Px $m ($ex) 7 0xff 0xff 0xff
    Px $m ($ex) 8 0x8b 0xe9 0xfd
}
# blush
Rect $m 3 10 2 1 0xff 0x9e 0xc4
Rect $m 11 10 2 1 0xff 0x9e 0xc4
# mouth w
Px $m 7 11 0xb0 0x56 0x8a; Px $m 8 11 0xb0 0x56 0x8a
$m.Save("$base\mascot.png", [System.Drawing.Imaging.ImageFormat]::Png); $m.Dispose()

# ---------- star.png (8x8 sparkle) ----------
$s = New-Bmp 8 8
Rect $s 3 0 2 8 0xff 0xff 0xff
Rect $s 0 3 8 2 0xff 0xff 0xff
Rect $s 3 3 2 2 0xff 0xb3 0xd9
foreach ($d in @(@(1,1),@(6,1),@(1,6),@(6,6))) { Px $s $d[0] $d[1] 0xff 0xd6 0xec }
$s.Save("$base\star.png", [System.Drawing.Imaging.ImageFormat]::Png); $s.Dispose()

# ---------- heart.png (8x8) ----------
$hh = New-Bmp 8 8
$heartRows = @(
    "  xx xx ",
    " xxxxxxx",
    "xxxxxxxx",
    "xxxxxxxx",
    " xxxxxx ",
    "  xxxx  ",
    "   xx   ",
    "        "
)
for ($y = 0; $y -lt 8; $y++) { for ($x = 0; $x -lt 8; $x++) {
    if ($heartRows[$y][$x] -eq 'x') { Px $hh $x $y 0xff 0x6f 0xb3 }
} }
Px $hh 2 1 0xff 0xd6 0xec
$hh.Save("$base\heart.png", [System.Drawing.Imaging.ImageFormat]::Png); $hh.Dispose()

Write-Output "generated:"
Get-ChildItem $base | Select-Object Name, Length
