package com.cricsaga.app.ui.utils

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.cricsaga.app.domain.model.InningsScoreState
import com.cricsaga.app.domain.model.MatchResult
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExportUtils {

    fun generateAndShareMatchPdf(
        context: Context,
        matchTitle: String,
        venue: String = "Grassroots Cricket Ground",
        battingTeamName: String,
        bowlingTeamName: String,
        scoreState: InningsScoreState,
        firstInningsScore: InningsScoreState? = null,
        firstInningsTeamName: String = "",
        matchResult: MatchResult? = null
    ) {
        val pageNumber = 1
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create() // A4 portrait in pt
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        // 1. Header Banner
        val headerPaint = Paint().apply {
            color = Color.rgb(17, 24, 39)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, 595f, 65f, headerPaint)

        val headerAccent = Paint().apply {
            color = Color.rgb(229, 57, 53) // Crimson accent line
            strokeWidth = 3f
        }
        canvas.drawLine(0f, 65f, 595f, 65f, headerAccent)

        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("CRICSAGA • OFFICIAL MATCH SCORESHEET", 25f, 28f, titlePaint)

        val subtitlePaint = Paint().apply {
            color = Color.rgb(209, 213, 219)
            textSize = 9f
            isAntiAlias = true
        }
        val dateStr = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
        canvas.drawText("Grassroots Digital Scoring Record  •  Date: $dateStr  •  Venue: $venue", 25f, 48f, subtitlePaint)

        // 2. Match Summary Bar
        val summaryBox = Paint().apply {
            color = Color.rgb(243, 244, 246)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(25f, 75f, 570f, 115f), 4f, 4f, summaryBox)

        val matchTitlePaint = Paint().apply {
            color = Color.rgb(17, 24, 39)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(matchTitle.uppercase(), 35f, 94f, matchTitlePaint)

        val resultPaint = Paint().apply {
            color = Color.rgb(180, 83, 9)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val resText = matchResult?.resultSummary?.ifEmpty { "Match in progress" } ?: "Match in progress"
        canvas.drawText("RESULT: $resText", 35f, 108f, resultPaint)

        // Helper Drawing Methods
        var currentY = 130f

        val tableHeaderPaint = Paint().apply {
            color = Color.rgb(31, 41, 55)
            style = Paint.Style.FILL
        }
        val tableHeaderText = Paint().apply {
            color = Color.WHITE
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            color = Color.rgb(31, 41, 55)
            textSize = 8f
            isAntiAlias = true
        }
        val textBold = Paint().apply {
            color = Color.rgb(17, 24, 39)
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val rowLinePaint = Paint().apply {
            color = Color.rgb(229, 231, 235)
            strokeWidth = 0.5f
        }

        fun drawInningsBlock(teamName: String, score: InningsScoreState, isFirstInnings: Boolean) {
            val sectionPaint = Paint().apply {
                color = Color.rgb(249, 250, 251)
                style = Paint.Style.FILL
            }
            canvas.drawRect(25f, currentY, 570f, currentY + 18f, sectionPaint)

            val sectionTitle = Paint().apply {
                color = Color.rgb(17, 24, 39)
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val innNum = if (isFirstInnings) "1ST INNINGS" else "2ND INNINGS"
            canvas.drawText(
                "$innNum: ${teamName.uppercase()} — ${score.totalRuns}/${score.totalWickets} (${score.oversFormatted} ov) • CRR: ${String.format("%.2f", score.currentRunRate)}",
                30f,
                currentY + 13f,
                sectionTitle
            )
            currentY += 22f

            // Batting Header
            canvas.drawRect(25f, currentY, 570f, currentY + 14f, tableHeaderPaint)
            canvas.drawText("BATSMAN", 30f, currentY + 10f, tableHeaderText)
            canvas.drawText("DISMISSAL", 170f, currentY + 10f, tableHeaderText)
            canvas.drawText("R", 380f, currentY + 10f, tableHeaderText)
            canvas.drawText("B", 415f, currentY + 10f, tableHeaderText)
            canvas.drawText("4s", 450f, currentY + 10f, tableHeaderText)
            canvas.drawText("6s", 485f, currentY + 10f, tableHeaderText)
            canvas.drawText("SR", 525f, currentY + 10f, tableHeaderText)
            currentY += 15f

            // Batting Rows (Top 6 or all)
            val batsmenList = score.batsmen.values.toList().take(7)
            for (b in batsmenList) {
                canvas.drawText(b.playerName, 30f, currentY + 9f, textBold)
                val disText = if (b.isOut) b.dismissal?.dismissalType?.name?.replace("_", " ")?.lowercase() ?: "out" else "not out"
                canvas.drawText(disText, 170f, currentY + 9f, textPaint)
                canvas.drawText("${b.runs}", 380f, currentY + 9f, textBold)
                canvas.drawText("${b.ballsFaced}", 415f, currentY + 9f, textPaint)
                canvas.drawText("${b.fours}", 450f, currentY + 9f, textPaint)
                canvas.drawText("${b.sixes}", 485f, currentY + 9f, textPaint)
                canvas.drawText(String.format("%.1f", b.strikeRate), 525f, currentY + 9f, textPaint)
                canvas.drawLine(25f, currentY + 12f, 570f, currentY + 12f, rowLinePaint)
                currentY += 13f
            }

            currentY += 4f

            // Bowling Header
            canvas.drawRect(25f, currentY, 570f, currentY + 14f, tableHeaderPaint)
            canvas.drawText("BOWLER", 30f, currentY + 10f, tableHeaderText)
            canvas.drawText("OVERS", 220f, currentY + 10f, tableHeaderText)
            canvas.drawText("MAIDENS", 300f, currentY + 10f, tableHeaderText)
            canvas.drawText("RUNS", 380f, currentY + 10f, tableHeaderText)
            canvas.drawText("WKTS", 450f, currentY + 10f, tableHeaderText)
            canvas.drawText("ECON", 525f, currentY + 10f, tableHeaderText)
            currentY += 15f

            // Bowling Rows
            val bowlersList = score.bowlers.values.toList().take(4)
            for (bw in bowlersList) {
                canvas.drawText(bw.bowlerName, 30f, currentY + 9f, textBold)
                canvas.drawText(bw.oversFormatted, 220f, currentY + 9f, textPaint)
                canvas.drawText("${bw.maidens}", 300f, currentY + 9f, textPaint)
                canvas.drawText("${bw.runsConceded}", 380f, currentY + 9f, textPaint)
                canvas.drawText("${bw.wickets}", 450f, currentY + 9f, textBold)
                canvas.drawText(String.format("%.2f", bw.economyRate), 525f, currentY + 9f, textPaint)
                canvas.drawLine(25f, currentY + 12f, 570f, currentY + 12f, rowLinePaint)
                currentY += 13f
            }

            currentY += 12f
        }

        // Render Innings 1
        val inn1Score = firstInningsScore ?: scoreState
        val inn1Team = if (firstInningsTeamName.isNotBlank()) firstInningsTeamName else battingTeamName
        drawInningsBlock(inn1Team, inn1Score, true)

        // Render Innings 2 (if present)
        if (firstInningsScore != null) {
            drawInningsBlock(battingTeamName, scoreState, false)
        }

        // 3. Player of the Match & Signatures Block
        val potmBox = Paint().apply {
            color = Color.rgb(254, 243, 199)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(25f, currentY, 570f, currentY + 36f), 4f, 4f, potmBox)

        val potmTitle = Paint().apply {
            color = Color.rgb(180, 83, 9)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val potmName = matchResult?.playerOfTheMatchName ?: "To be decided"
        canvas.drawText("PLAYER OF THE MATCH: ${potmName.uppercase()}", 35f, currentY + 16f, potmTitle)

        val potmSub = Paint().apply {
            color = Color.rgb(146, 64, 14)
            textSize = 8f
            isAntiAlias = true
        }
        canvas.drawText("Honoured for outstanding match-winning grassroots contribution", 35f, currentY + 28f, potmSub)

        currentY += 50f

        // Signature Lines
        val sigLine = Paint().apply {
            color = Color.rgb(156, 163, 175)
            strokeWidth = 1f
        }
        val sigText = Paint().apply {
            color = Color.rgb(107, 114, 128)
            textSize = 8f
            isAntiAlias = true
        }

        canvas.drawLine(40f, currentY + 20f, 180f, currentY + 20f, sigLine)
        canvas.drawText("Official Scorer Signature", 55f, currentY + 32f, sigText)

        canvas.drawLine(230f, currentY + 20f, 370f, currentY + 20f, sigLine)
        canvas.drawText("Umpire 1 Signature", 260f, currentY + 32f, sigText)

        canvas.drawLine(415f, currentY + 20f, 555f, currentY + 20f, sigLine)
        canvas.drawText("Umpire 2 / Captain Signature", 430f, currentY + 32f, sigText)

        // Footer Stamp
        val footerPaint = Paint().apply {
            color = Color.rgb(156, 163, 175)
            textSize = 7.5f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Generated by CricSaga Grassroots Cricket Platform  •  Verified Offline-First Digital Ledger", 297f, 825f, footerPaint)

        pdfDoc.finishPage(page)

        // Save PDF to cache and dispatch Intent
        val docsDir = File(context.cacheDir, "docs").apply { if (!exists()) mkdirs() }
        val pdfFile = File(docsDir, "CricSaga_Scoresheet_${System.currentTimeMillis()}.pdf")
        FileOutputStream(pdfFile).use { out ->
            pdfDoc.writeTo(out)
        }
        pdfDoc.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Official Match Scoresheet: $matchTitle")
            putExtra(Intent.EXTRA_TEXT, "Official A4 match scoresheet for $matchTitle, scored on CricSaga.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share A4 Match Scoresheet (PDF)"))
    }
}
