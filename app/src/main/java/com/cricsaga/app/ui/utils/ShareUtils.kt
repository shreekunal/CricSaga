package com.cricsaga.app.ui.utils

import android.content.Context
import android.content.Intent
import android.graphics.*
import androidx.core.content.FileProvider
import com.cricsaga.app.domain.model.InningsScoreState
import com.cricsaga.app.domain.model.MatchResult
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object ShareUtils {

    fun generateAndSharePoster(
        context: Context,
        matchTitle: String,
        battingTeamName: String,
        bowlingTeamName: String,
        scoreState: InningsScoreState,
        firstInningsScore: InningsScoreState? = null,
        firstInningsTeamName: String? = null,
        matchResult: MatchResult? = null
    ) {
        val width = 1080
        val height = 1350
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        val bgPaint = Paint().apply {
            color = Color.rgb(15, 16, 19)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Card container
        val cardPaint = Paint().apply {
            color = Color.rgb(23, 25, 30)
            style = Paint.Style.FILL
        }
        val cardRect = RectF(60f, 60f, (width - 60).toFloat(), (height - 60).toFloat())
        canvas.drawRoundRect(cardRect, 32f, 32f, cardPaint)

        // Border
        val borderPaint = Paint().apply {
            color = Color.rgb(38, 41, 51)
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(cardRect, 32f, 32f, borderPaint)

        // Header Title: CRICSAGA
        val brandPaint = Paint().apply {
            color = Color.rgb(229, 57, 53)
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("CRICSAGA • HAR RUN KI KAHANI", width / 2f, 150f, brandPaint)

        // Match Title
        val matchTitlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 52f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(matchTitle, width / 2f, 240f, matchTitlePaint)

        // Result Banner or Live Banner
        val resultText = matchResult?.resultSummary?.ifEmpty { "MATCH SUMMARY" } ?: "MATCH SUMMARY"
        val bannerPaint = Paint().apply {
            color = Color.rgb(255, 179, 0)
            textSize = 38f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(resultText, width / 2f, 320f, bannerPaint)

        // Divider
        val dividerPaint = Paint().apply {
            color = Color.rgb(38, 41, 51)
            strokeWidth = 2f
        }
        canvas.drawLine(120f, 380f, (width - 120).toFloat(), 380f, dividerPaint)

        // Innings 1 Section
        val inn1Name = firstInningsTeamName ?: battingTeamName
        val inn1Score = firstInningsScore ?: scoreState

        val teamNamePaint = Paint().apply {
            color = Color.rgb(209, 213, 219)
            textSize = 42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(inn1Name.uppercase(), 120f, 470f, teamNamePaint)

        val scorePaint = Paint().apply {
            color = Color.WHITE
            textSize = 72f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("${inn1Score.totalRuns}/${inn1Score.totalWickets}", 120f, 560f, scorePaint)

        val oversPaint = Paint().apply {
            color = Color.rgb(156, 163, 175)
            textSize = 34f
            isAntiAlias = true
        }
        canvas.drawText("${inn1Score.oversFormatted} ov • CRR: ${String.format(Locale.US, "%.2f", inn1Score.currentRunRate)}", 120f, 620f, oversPaint)

        // Innings 2 Section (if available)
        if (firstInningsScore != null) {
            canvas.drawLine(120f, 690f, (width - 120).toFloat(), 690f, dividerPaint)

            canvas.drawText(battingTeamName.uppercase(), 120f, 780f, teamNamePaint)
            canvas.drawText("${scoreState.totalRuns}/${scoreState.totalWickets}", 120f, 870f, scorePaint)
            canvas.drawText("${scoreState.oversFormatted} ov • CRR: ${String.format(Locale.US, "%.2f", scoreState.currentRunRate)}", 120f, 930f, oversPaint)
        }

        // Top Performers section
        val topBatter = scoreState.batsmen.values.maxByOrNull { it.runs }
        val topBowler = scoreState.bowlers.values.maxByOrNull { it.wickets }

        canvas.drawLine(120f, 1010f, (width - 120).toFloat(), 1010f, dividerPaint)

        val statHeaderPaint = Paint().apply {
            color = Color.rgb(255, 179, 0)
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("TOP PERFORMERS", 120f, 1070f, statHeaderPaint)

        val statValuePaint = Paint().apply {
            color = Color.WHITE
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val batterText = topBatter?.let { "${it.playerName}: ${it.runs} (${it.ballsFaced}b) • ${it.fours}x4 ${it.sixes}x6" } ?: "Batted with flair"
        canvas.drawText("BAT  $batterText", 120f, 1130f, statValuePaint)

        val bowlerText = topBowler?.let { "${it.bowlerName}: ${it.wickets}/${it.runsConceded} (${it.oversFormatted} ov)" } ?: "Bowled with discipline"
        canvas.drawText("BOWL $bowlerText", 120f, 1190f, statValuePaint)

        // Footer
        val footerPaint = Paint().apply {
            color = Color.rgb(156, 163, 175)
            textSize = 26f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Scored with CricSaga — Grassroots Cricket Platform", width / 2f, 1280f, footerPaint)

        // Save bitmap to cache and dispatch Intent
        try {
            val imagesFolder = File(context.cacheDir, "images")
            imagesFolder.mkdirs()
            val file = File(imagesFolder, "match_poster.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, "$matchTitle\n$resultText\n\nScored on CricSaga")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Match Poster"))
        } catch (e: Exception) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "$matchTitle\n$resultText\n\nScored on CricSaga")
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Match Result"))
        }
    }

    fun generateAndShareAthleteCard(
        context: Context,
        playerName: String,
        jerseyNumber: String,
        archetype: String,
        city: String,
        matches: Int,
        runs: Int,
        wickets: Int,
        average: String,
        profilePhotoPath: String? = null
    ) {
        val width = 1080
        val height = 1350
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Dark Canvas
        val bgPaint = Paint().apply {
            color = Color.rgb(10, 11, 14)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Card container
        val cardPaint = Paint().apply {
            color = Color.rgb(20, 23, 31)
            style = Paint.Style.FILL
        }
        val cardRect = RectF(60f, 60f, (width - 60).toFloat(), (height - 60).toFloat())
        canvas.drawRoundRect(cardRect, 32f, 32f, cardPaint)

        // Border
        val borderPaint = Paint().apply {
            color = Color.rgb(34, 39, 51)
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(cardRect, 32f, 32f, borderPaint)

        // Giant Jersey Watermark in Background
        val watermarkPaint = Paint().apply {
            color = Color.argb(18, 255, 255, 255)
            textSize = 340f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText("#$jerseyNumber", (width - 100).toFloat(), 420f, watermarkPaint)

        // Optional circular athlete photo avatar
        val photoBitmap = ImageUtils.loadAndroidBitmapFromPath(profilePhotoPath)
        if (photoBitmap != null) {
            val avatarRadius = 75f
            val avatarCenterX = (width - 200).toFloat()
            val avatarCenterY = 240f
            val clipPath = android.graphics.Path().apply {
                addCircle(avatarCenterX, avatarCenterY, avatarRadius, android.graphics.Path.Direction.CCW)
            }
            canvas.save()
            canvas.clipPath(clipPath)
            val srcRect = android.graphics.Rect(0, 0, photoBitmap.width, photoBitmap.height)
            val dstRect = RectF(avatarCenterX - avatarRadius, avatarCenterY - avatarRadius, avatarCenterX + avatarRadius, avatarCenterY + avatarRadius)
            canvas.drawBitmap(photoBitmap, srcRect, dstRect, null)
            canvas.restore()

            val photoRingPaint = Paint().apply {
                color = Color.rgb(229, 57, 53)
                style = Paint.Style.STROKE
                strokeWidth = 4f
                isAntiAlias = true
            }
            canvas.drawCircle(avatarCenterX, avatarCenterY, avatarRadius, photoRingPaint)
        }

        // Brand Title
        val brandPaint = Paint().apply {
            color = Color.rgb(229, 57, 53)
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("CRICSAGA • OFFICIAL ATHLETE CARD", 120f, 160f, brandPaint)

        // Player Name
        val namePaint = Paint().apply {
            color = Color.WHITE
            textSize = 64f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(playerName.uppercase(), 120f, 260f, namePaint)

        // Archetype & City
        val metaPaint = Paint().apply {
            color = Color.rgb(217, 119, 6)
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("[ $archetype ]  •  $city", 120f, 330f, metaPaint)

        // Divider
        val dividerPaint = Paint().apply {
            color = Color.rgb(34, 39, 51)
            strokeWidth = 2f
        }
        canvas.drawLine(120f, 400f, (width - 120).toFloat(), 400f, dividerPaint)

        // 4 Key Stats Quad-Strip
        val labelPaint = Paint().apply {
            color = Color.rgb(156, 163, 175)
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            color = Color.WHITE
            textSize = 58f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        // Column X coordinates
        val col1 = 140f
        val col2 = 380f
        val col3 = 620f
        val col4 = 840f

        canvas.drawText("MATCHES", col1, 480f, labelPaint)
        canvas.drawText("$matches", col1, 560f, valuePaint)

        canvas.drawText("RUNS", col2, 480f, labelPaint)
        canvas.drawText("$runs", col2, 560f, valuePaint)

        canvas.drawText("WICKETS", col3, 480f, labelPaint)
        canvas.drawText("$wickets", col3, 560f, valuePaint)

        canvas.drawText("BAT AVG", col4, 480f, labelPaint)
        canvas.drawText(average, col4, 560f, valuePaint)

        canvas.drawLine(120f, 630f, (width - 120).toFloat(), 630f, dividerPaint)

        // Honours Shelf
        val shelfHeaderPaint = Paint().apply {
            color = Color.rgb(217, 119, 6)
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("CAREER HONOURS & MILESTONES", 120f, 710f, shelfHeaderPaint)

        val honourPaint = Paint().apply {
            color = Color.rgb(243, 244, 246)
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        canvas.drawText("TROPHY  Century Club Member", 120f, 790f, honourPaint)
        canvas.drawText("TARGET   5-Wicket Haul Achieved", 120f, 860f, honourPaint)
        canvas.drawText("FLASH     Super Striker (SR 140+)", 120f, 930f, honourPaint)
        canvas.drawText("SHIELD    Safe Hands (Fielding Milestone)", 120f, 1000f, honourPaint)

        // Footer
        val footerPaint = Paint().apply {
            color = Color.rgb(156, 163, 175)
            textSize = 28f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Har Run Ki Kahani — CricSaga Grassroots Cricket Platform", width / 2f, 1260f, footerPaint)

        try {
            val imagesFolder = File(context.cacheDir, "images")
            imagesFolder.mkdirs()
            val file = File(imagesFolder, "athlete_card.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, "My Official CricSaga Athlete Card — $playerName (#$jerseyNumber)\nTrack your cricket journey on CricSaga")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Athlete Card"))
        } catch (_: Exception) {}
    }
}
